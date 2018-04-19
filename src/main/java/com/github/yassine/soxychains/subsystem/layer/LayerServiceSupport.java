package com.github.yassine.soxychains.subsystem.layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ServiceScope;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulNamingUtils.namespaceLayerService;
import static com.machinezoo.noexception.Exceptions.sneak;
import static io.reactivex.Observable.fromFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class LayerServiceSupport implements LayerService {

  private final Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> providers;
  private final SoxyChainsConfiguration soxyChainsConfiguration;
  private final LayerManager layerManager;
  private final DockerConfiguration dockerConfiguration;
  private final DockerProvider dockerProvider;
  private final ObjectMapper objectMapper;

  @Override
  public Single<Boolean> add(LayerNode node) {
    AbstractLayerConfiguration layerConfiguration = soxyChainsConfiguration.getLayers().get(node.getLayerIndex());
    LayerProvider provider = providers.get(layerConfiguration.getClass());
    return layerManager.findCapableHost(node.getLayerIndex())
      .flatMap(docker -> {
        Maybe<Network> network = docker.findNetwork(nameSpaceLayerNetwork(dockerConfiguration, node.getLayerIndex()));
        String random = randomName();
        String layerConfigString = objectMapper.writeValueAsString(layerConfiguration);
        String nodeConfigString  = objectMapper.writeValueAsString(node.getNodeConfiguration());
        return docker.runContainer(
          namespaceLayerNode(dockerConfiguration, node.getLayerIndex(), random),
          nameSpaceImage(dockerConfiguration, provider.image(layerConfiguration).getName()),
          createContainerCmd -> {
            provider.configureNode(createContainerCmd, node.getNodeConfiguration(), layerConfiguration);
            createContainerCmd
              .withNetworkMode(network.blockingGet().getName())
              .withLabels(
                ImmutableMap.<String,String>builder()
                  .putAll(Optional.ofNullable(createContainerCmd.getLabels()).orElse(ImmutableMap.of()))
                  .putAll(labelizeLayerNode(provider.getClass(), node.getLayerIndex(), soxyChainsConfiguration.getDocker(), random))
                  .put(getConfigLabelOfLayerProvider(provider.getClass()), layerConfigString)
                  .put(getConfigLabelOfLayerNode(provider.getClass()), nodeConfigString)
                  .put(LAYER_NODE_LABEL, "")
                  .put(LAYER_SERVICE_KEY_LABEL, namespaceLayerService(node.getLayerIndex(), ServiceScope.LOCAL))
                  .build()
              );
          }
        ).map(Objects::nonNull);
      })
      .defaultIfEmpty(false)
      .toSingle();
  }

  @Override
  public Single<Boolean> remove(LayerNode node) {
    AbstractLayerConfiguration layerConfiguration = soxyChainsConfiguration.getLayers().get(node.getLayerIndex());
    LayerProvider provider = providers.get(layerConfiguration.getClass());
    return Observable.fromIterable(dockerProvider.clients())
      .flatMap(docker ->
        fromFuture(supplyAsync(() -> docker.listContainersCmd().withLabelFilter(filterLayerNode(provider.getClass(), node.getLayerIndex(), dockerConfiguration)).exec()))
          .subscribeOn(Schedulers.io())
          .flatMap(Observable::fromIterable)
          .filter(container -> sneak().get(() -> objectMapper.readValue(container.getLabels().get(getConfigLabelOfLayerNode(provider.getClass())), node.getNodeConfiguration().getClass())).equals(node.getNodeConfiguration()))
          .map(container -> Pair.of(docker, container))
      )
      .take(1)
      .flatMapMaybe(pair -> dockerProvider.get(pair.getKey().configuration())
                              .stopContainer(namespaceLayerNode(dockerConfiguration, node.getLayerIndex(), pair.getValue().getLabels().get(RANDOM_LABEL))))
      .take(1)
      .single(false);
  }

  private String randomName(){
    return UUID.randomUUID().toString().substring(0, 8);
  }

}

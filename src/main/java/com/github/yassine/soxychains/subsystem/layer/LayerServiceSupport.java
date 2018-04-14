package com.github.yassine.soxychains.subsystem.layer;

import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
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
import java.util.UUID;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class LayerServiceSupport implements LayerService {

  private final Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> providers;
  private final SoxyChainsConfiguration soxyChainsConfiguration;
  private final LayerManager layerManager;
  private final DockerConfiguration dockerConfiguration;
  private final DockerProvider dockerProvider;

  @Override
  public Single<Boolean> add(LayerNode node) {
    AbstractLayerConfiguration layerConfiguration = soxyChainsConfiguration.getLayers().get(node.getLayerIndex());
    LayerProvider provider = providers.get(layerConfiguration.getClass());
    return layerManager.findCapableHost(node.getLayerIndex())
      .flatMap(docker -> {
        Maybe<Network> network = docker.findNetwork(nameSpaceLayerNetwork(dockerConfiguration, node.getLayerIndex()));
        String random = randomName();
        return docker.runContainer(
          namespaceLayerNode(dockerConfiguration, node.getLayerIndex(), randomName()),
          nameSpaceImage(dockerConfiguration, provider.image().getName()),
          (createContainerCmd) -> {
            provider.configureNode(createContainerCmd, node.getNodeConfiguration(), layerConfiguration);
            createContainerCmd
              .withNetworkMode(network.blockingGet().getName())
              .withLabels(
                ImmutableMap.<String,String>builder()
                  .putAll(labelizeLayerNode(provider.getClass(), node.getLayerIndex(), soxyChainsConfiguration.getDocker()))
                  .put(RANDOM_LABEL, random)
                  .build()
              );
          },
          (containerID) -> {},
          (startContainerCmd) -> {},
          (containerID) -> {}
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
        fromFuture(supplyAsync(() -> docker.listContainersCmd().withLabelFilter(labelizeLayerNode(provider.getClass(), node.getLayerIndex(), dockerConfiguration)).exec()))
          .subscribeOn(Schedulers.io())
          .flatMap(Observable::fromIterable)
          .filter(container -> provider.matches(container, node.getNodeConfiguration(), layerConfiguration))
          .map(container -> Pair.of(docker, container))
      )
      .take(1)
      .flatMapMaybe(pair -> dockerProvider.get(pair.getKey().configuration())
                              .stopContainer(namespaceLayerNode(dockerConfiguration, node.getLayerIndex(), pair.getValue().getLabels().get(RANDOM_LABEL)),
                                (stopContainerCmd)->{},
                                (containerID)->{},
                                (removeContainerCmd)->{},
                                (containerID)->{}))
      .take(1)
      .single(false);
  }

  private String randomName(){
    return UUID.randomUUID().toString().substring(0, 10);
  }

}

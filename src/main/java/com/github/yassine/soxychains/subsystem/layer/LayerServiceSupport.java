package com.github.yassine.soxychains.subsystem.layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.subsystem.docker.client.Docker;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkHelper;
import com.github.yassine.soxychains.subsystem.service.consul.ServiceScope;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.namespaceLayerService;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.portShift;
import static com.machinezoo.noexception.Exceptions.sneak;
import static io.reactivex.Observable.fromFuture;
import static io.reactivex.Observable.fromIterable;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class LayerServiceSupport implements LayerService {

  private final Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> providers;
  private final SoxyChainsContext soxyChainsContext;
  private final LayerManager layerManager;
  private final DockerContext dockerContext;
  private final DockerProvider dockerProvider;
  private final ObjectMapper objectMapper;
  private final NetworkHelper networkHelper;
  private final Set<LayerObserver> layerObservers;

  @Override
  public Single<Boolean> add(LayerNode node) {
    AbstractLayerConfiguration layerConfiguration = node.getLayerConfiguration();
    LayerProvider provider = providers.get(layerConfiguration.getClass());
    return layerManager.findCapableHost(node.getLayerIndex())
      .flatMap(docker -> {
        Maybe<Network> network = docker.findNetwork(nameSpaceLayerNetwork(dockerContext, node.getLayerIndex()));
        String random = randomName();
        String layerConfigString = objectMapper.writeValueAsString(layerConfiguration);
        String nodeConfigString  = objectMapper.writeValueAsString(node.getNodeConfiguration());
        return docker.runContainer(
          namespaceLayerNode(dockerContext, node.getLayerIndex(), random),
          nameSpaceImage(dockerContext, provider.image(layerConfiguration).getName()),
          createContainerCmd -> {
            provider.configureNode(createContainerCmd, node.getNodeConfiguration(), layerConfiguration);
            createContainerCmd
              .withNetworkMode(network.blockingGet().getName())
              .withLabels(
                ImmutableMap.<String,String>builder()
                  .putAll(Optional.ofNullable(createContainerCmd.getLabels()).orElse(ImmutableMap.of()))
                  .putAll(labelizeLayerNode(provider.getClass(), node.getLayerIndex(), dockerContext, random))
                  .put(getConfigLabelOfLayerProvider(provider.getClass()), layerConfigString)
                  .put(getConfigLabelOfLayerNode(provider.getClass()), nodeConfigString)
                  .put(LAYER_NODE_LABEL, "")
                  .put(LAYER_SERVICE_KEY_LABEL, namespaceLayerService(node.getLayerIndex(), ServiceScope.LOCAL))
                  .build()
              );
            networkHelper.getDNSAddressAtLayer(docker, node.getLayerIndex()).map(createContainerCmd::withDns).toObservable().blockingSubscribe();
          }
        ).map(Objects::nonNull);
      })
      .defaultIfEmpty(false)
      .toSingle();
  }

  @Override
  public Single<Boolean> remove(LayerNode node) {
    AbstractLayerConfiguration layerConfiguration = node.getLayerConfiguration();
    LayerProvider provider = providers.get(layerConfiguration.getClass());
    return dockerProvider.clients()
      .flatMap(docker ->
        fromFuture(supplyAsync(() -> docker.listContainersCmd().withLabelFilter(filterLayerNode(provider.getClass(), node.getLayerIndex(), dockerContext)).exec()))
          .subscribeOn(Schedulers.io())
          .flatMap(Observable::fromIterable)
          .filter(container -> sneak().get(() -> objectMapper.readValue(container.getLabels().get(getConfigLabelOfLayerNode(provider.getClass())), node.getNodeConfiguration().getClass())).equals(node.getNodeConfiguration()))
          .map(container -> Pair.of(docker, container))
      )
      .take(1)
      .flatMapMaybe(pair -> dockerProvider.get(pair.getKey().configuration())
                              .stopContainer(namespaceLayerNode(dockerContext, node.getLayerIndex(), pair.getValue().getLabels().get(RANDOM_LABEL))))
      .take(1)
      .single(false);
  }

  public Single<Boolean> addLayer(int index, AbstractLayerConfiguration layerConfiguration){
    //create a network
    return dockerProvider.dockers()
      .flatMapMaybe(docker -> docker.createNetwork(nameSpaceLayerNetwork(dockerContext, index),
              createNetworkCmd -> configureNetworkOptions(index - 1, createNetworkCmd, docker) )
        .flatMap(networkId ->
          //create the layer network
          docker.findNetwork(nameSpaceLayerNetwork(dockerContext, index))
            .flatMapSingle(network -> fromIterable(layerObservers)
              // notify the plugins
              .flatMapMaybe(plugin -> plugin.onLayerAdd(index, layerConfiguration, network)
                                        .subscribeOn(Schedulers.io()))
              .reduce(true, AND_OPERATOR)
            ).toMaybe()
        )).reduce(true, AND_OPERATOR);
  }

  public Single<Boolean> removeLayer(int index, AbstractLayerConfiguration layerConfiguration){
    return dockerProvider.dockers()
      .flatMapMaybe(docker ->
        docker.findNetwork(nameSpaceLayerNetwork(dockerContext, index)).toObservable()
          .flatMap(network -> fromIterable(layerObservers)
            // notify the plugins
            .flatMapMaybe(plugin -> plugin.onLayerPreRemove(index, layerConfiguration, network).subscribeOn(Schedulers.io()))
            .defaultIfEmpty(true)
          ).reduce(true, AND_OPERATOR)
          //remove the network
          .flatMapMaybe(result -> docker.removeNetwork(nameSpaceLayerNetwork(dockerContext, index)).subscribeOn(Schedulers.io()))
          .defaultIfEmpty(true)
      )
      .defaultIfEmpty(true)
      .reduce(true, AND_OPERATOR);
  }

  private CreateNetworkCmd configureNetworkOptions(int upperLayerIndex, CreateNetworkCmd createNetworkCmd, Docker docker){
    if(upperLayerIndex >= 0){
      AbstractLayerConfiguration upperLayerConfiguration = soxyChainsContext.getLayers().get(upperLayerIndex);
      networkHelper.getGobetweenAddress(docker)
        .toObservable()
        .blockingSubscribe(gobetweenAddress -> createNetworkCmd.withOptions(
          ImmutableMap.of(
            NetworkHelper.SOXY_DRIVER_PROXY_HOST_OPTION, gobetweenAddress,
            NetworkHelper.SOXY_DRIVER_PROXY_PORT_OPTION, Integer.toString(portShift(upperLayerIndex, upperLayerConfiguration.getClusterServicePort()))
          )
        ));
    }
    createNetworkCmd.withDriver(soxyDriverName(dockerContext))
      .withLabels(labelizeLayerEntity(upperLayerIndex + 1));
    return createNetworkCmd;
  }

  private String randomName(){
    return randomUUID().toString().substring(0, 8);
  }

}

package com.github.yassine.soxychains.subsystem.layer;

import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.client.Docker;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.client.SoxyChainsDockerClient;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.labelizeLayerNode;
import static io.reactivex.Observable.fromIterable;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class LayerManager {

  private final SoxyChainsConfiguration soxyChainsConfiguration;
  private final DockerProvider dockerProvider;
  private final Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> layerProviders;

  /**
   * Finds a host where it is possible to add a node at a specific tunneling layer/level
   * @param layerIndex
   * @return
   */
  Maybe<Docker> findCapableHost(final int layerIndex){
    AbstractLayerConfiguration layerConfiguration = soxyChainsConfiguration.getLayers().get(layerIndex);
    LayerProvider provider = layerProviders.get(layerConfiguration.getClass());
    return Maybe.just(soxyChainsConfiguration.getLayers().get(layerIndex))
            .flatMapObservable(layerConfig -> fromIterable(dockerProvider.clients())
              .flatMapSingle(dockerClient -> isCapable(dockerClient, layerConfig, provider, layerIndex).map(result -> Pair.of(dockerClient, result)))
              .filter(Pair::getValue)
              .map(Pair::getKey)
              .take(1))
            .firstElement()
            .map(dockerClient -> dockerProvider.get(dockerClient.configuration()));
  }

  private Single<Boolean> isCapable(SoxyChainsDockerClient client, AbstractLayerConfiguration layerConfiguration, LayerProvider provider, int layerIndex){
    return Single.fromFuture(CompletableFuture.supplyAsync(() -> {
      int nodeCount = client.listContainersCmd()
        .withLabelFilter(
          labelizeLayerNode(provider.getClass(), layerIndex, soxyChainsConfiguration.getDocker())
        ).exec().size();
      int maxNodeCount = allocation(soxyChainsConfiguration.getDocker(), layerConfiguration);
      return nodeCount < maxNodeCount;
    }));
  }

  private int allocation(DockerConfiguration dockerConfiguration, AbstractLayerConfiguration layerConfiguration){
    int nodesCount = layerConfiguration.getMaxNodes();
    int hostCount = dockerConfiguration.getHosts().size();
    return nodesCount/hostCount;
  }

}
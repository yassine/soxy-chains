package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.gobetween.GobetweenClient;
import com.github.yassine.gobetween.api.configuration.service.TCPServiceConfiguration;
import com.github.yassine.gobetween.api.configuration.service.discovery.ConsulServiceDiscovery;
import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ServiceScope;
import io.reactivex.Single;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulNamingUtils.namespaceLayerService;
import static io.reactivex.Single.fromFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor @Accessors(fluent = true) @Slf4j
public class GobetweenSupport implements Gobetween {

  private final GobetweenClient gobetweenClient;
  private final DockerConfiguration dockerConfiguration;
  @Getter
  private final DockerHostConfiguration host;
  private final ConsulConfiguration consulConfiguration;

  @Override
  public Single<Boolean> register(int layerIndex, AbstractLayerConfiguration layerConfiguration) {
    return fromFuture(supplyAsync( () -> {
      try{
        final String localServiceName   = namespaceLayerService(layerIndex, ServiceScope.LOCAL);
        final String clusterServiceName = namespaceLayerService(layerIndex, ServiceScope.CLUSTER);
        //Defining the host-local service
        gobetweenClient.addService(
          localServiceName,
          new TCPServiceConfiguration()
            .setBind(":"+portShift(layerIndex, layerConfiguration.getClusterServicePort()))
            .setBalance("roundrobin")
            .setMaxConnections(0)
            .setClientIdleTimeout("10m")
            .setBackendIdleTimeout("10m")
            .setBackendConnectionTimeout("60s")
            .setDiscovery(new ConsulServiceDiscovery()
              .setConsulHost(nameSpaceContainer(dockerConfiguration, consulConfiguration.serviceName()))
              .setConsulServiceTag(localServiceName)
              .setConsulServiceName(localServiceName)
              .setConsulServicePassingOnly(true)
              .setInterval(String.format("%s",layerConfiguration.getHealthCheckInterval()/2))
            )
        );
        //Defining the cluster-wide service
        gobetweenClient.addService(
          clusterServiceName,
          new TCPServiceConfiguration()
            .setBind(":"+portShift(layerIndex, layerConfiguration.getClusterServicePort()))
            .setBalance("roundrobin")
            .setMaxConnections(0)
            .setClientIdleTimeout("10m")
            .setBackendIdleTimeout("10m")
            .setBackendConnectionTimeout("60s")
            .setDiscovery(new ConsulServiceDiscovery()
              .setConsulHost(nameSpaceContainer(dockerConfiguration, consulConfiguration.serviceName()))
              .setConsulServiceTag(clusterServiceName)
              .setConsulServiceName(clusterServiceName)
              .setConsulServicePassingOnly(true)
              .setInterval(String.format("%s",layerConfiguration.getHealthCheckInterval()/2))
            )
        );
        return true;
      }catch (Exception e){
        log.error(e.getMessage(), e);
        return false;
      }
    }));
  }

  private int portShift(int layerIndex, int port){
    return port + layerIndex * 3;
  }

  @Override
  public Single<Boolean> unRegister(int layerIndex, AbstractLayerConfiguration layerConfiguration) {
    return null;
  }

}

package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.gobetween.GobetweenClient;
import com.github.yassine.gobetween.api.configuration.ServersConfiguration;
import com.github.yassine.gobetween.api.configuration.service.TCPServiceConfiguration;
import com.github.yassine.gobetween.api.configuration.service.discovery.ConsulServiceDiscovery;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerContext;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils;
import com.github.yassine.soxychains.subsystem.service.consul.ServiceScope;
import io.reactivex.Single;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.namespaceLayerService;
import static io.reactivex.Single.fromFuture;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor @Accessors(fluent = true) @Slf4j
public class GobetweenSupport implements Gobetween {

  private final GobetweenClient gobetweenClient;
  private final DockerContext dockerContext;
  @Getter
  private final DockerHostConfiguration host;
  private final ConsulConfiguration consulConfiguration;

  @Override
  public Single<Boolean> register(int layerIndex, AbstractLayerContext layerConfiguration) {
    return fromFuture(supplyAsync( () -> {
      try{
        final String localServiceName   = namespaceLayerService(layerIndex, ServiceScope.LOCAL);
        final String clusterServiceName = namespaceLayerService(layerIndex, ServiceScope.CLUSTER);
        final ServersConfiguration serversConfiguration = gobetweenClient.getServers();
        if(!serversConfiguration.containsKey(localServiceName)){
          //Defining the host-local service
          gobetweenClient.addService(
            localServiceName,
            new TCPServiceConfiguration()
              .setBind(":"+ConsulUtils.portShift(layerIndex, layerConfiguration.getLocalServicePort()))
              .setBalance("roundrobin")
              .setMaxConnections(0)
              .setClientIdleTimeout("10m")
              .setBackendIdleTimeout("10m")
              .setBackendConnectionTimeout("60s")
              .setDiscovery(new ConsulServiceDiscovery()
                .setConsulHost(format("%s:%s",nameSpaceContainer(dockerContext, consulConfiguration.serviceName()), consulConfiguration.getServicePort()))
                .setConsulServiceTag(localServiceName)
                .setConsulServiceName(localServiceName)
                .setConsulServicePassingOnly(true)
                .setInterval(format("%ss",layerConfiguration.getHealthCheckInterval()/2))
              )
          );
        }
        if(!serversConfiguration.containsKey(clusterServiceName)){
          //Defining the cluster-wide service
          gobetweenClient.addService(
            clusterServiceName,
            new TCPServiceConfiguration()
              .setBind(":"+ ConsulUtils.portShift(layerIndex, layerConfiguration.getClusterServicePort()))
              .setBalance("roundrobin")
              .setMaxConnections(0)
              .setClientIdleTimeout("10m")
              .setBackendIdleTimeout("10m")
              .setBackendConnectionTimeout("60s")
              .setDiscovery(new ConsulServiceDiscovery()
                .setConsulHost(format("%s:%s",nameSpaceContainer(dockerContext, consulConfiguration.serviceName()), consulConfiguration.getServicePort()))
                .setConsulServiceTag(clusterServiceName)
                .setConsulServiceName(clusterServiceName)
                .setConsulServicePassingOnly(true)
                .setInterval(format("%ss",layerConfiguration.getHealthCheckInterval()/2))
              )
          );
        }
        return true;
      }catch (Exception e){
        log.error(e.getMessage(), e);
        return false;
      }
    }));
  }

  @Override
  public Single<Boolean> unRegister(int layerIndex, AbstractLayerContext layerConfiguration) {
    return fromFuture(supplyAsync( () -> {
      try{
        final String localServiceName   = namespaceLayerService(layerIndex, ServiceScope.LOCAL);
        final String clusterServiceName = namespaceLayerService(layerIndex, ServiceScope.CLUSTER);
        final ServersConfiguration serversConfiguration = gobetweenClient.getServers();
        if(serversConfiguration.containsKey(localServiceName)){
          //Removing the host-local service
          gobetweenClient.removeService(localServiceName);
        }
        if(serversConfiguration.containsKey(clusterServiceName)){
          //Defining the cluster-wide service
          gobetweenClient.removeService(clusterServiceName);
        }
        return true;
      }catch (Exception e){
        log.error(e.getMessage(), e);
        return false;
      }
    }));
  }

}

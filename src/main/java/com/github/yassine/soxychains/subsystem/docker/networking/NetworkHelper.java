package com.github.yassine.soxychains.subsystem.docker.networking;


import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.yassine.soxychains.subsystem.docker.client.Docker;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Maybe;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC) @Singleton
public class NetworkHelper {

  public static final String SOXY_DRIVER_PROXY_HOST_OPTION = "soxy.proxyaddress";
  public static final String SOXY_DRIVER_PROXY_PORT_OPTION = "soxy.proxyport";

  private final DockerContext dockerContext;
  private final GobetweenConfiguration gobetweenConfiguration;

  public Maybe<String> getContainerAddress(Docker docker, String containerName){
    return docker.findContainer(containerName)
      .flatMap(container ->
        ofNullable(container.getNetworkSettings())
          .map(ContainerNetworkSettings::getNetworks)
          .map(networks -> networks.get(nameSpaceNetwork(dockerContext, dockerContext.getNetworkingConfiguration().getNetworkName())).getIpAddress())
          .map(Maybe::just)
          .orElse(Maybe.empty())
      );
  }

  public Maybe<String> getContainerAddressAtLayer(Docker docker, String containerName, Integer layerIndex){
    return docker.findContainer(containerName)
      .flatMap(container ->
        ofNullable(container.getNetworkSettings())
          .map(ContainerNetworkSettings::getNetworks)
          .map(networks -> networks.get(nameSpaceLayerNetwork(dockerContext, layerIndex)).getIpAddress())
          .map(Maybe::just)
          .orElse(Maybe.empty())
      );
  }

  public Maybe<String> getGobetweenAddress(Docker docker){
    return getContainerAddress(docker, nameSpaceContainer(dockerContext, gobetweenConfiguration.getServiceName()));
  }

  public Maybe<String> getDNSAddress(Docker docker){
    return getContainerAddress(docker, nameSpaceContainer(dockerContext, dockerContext.getNetworkingConfiguration().getDnsConfiguration().getServiceName()));
  }

  public Maybe<String> getDNSAddressAtLayer(Docker docker, Integer layerIndex){
    return getContainerAddressAtLayer(docker, nameSpaceContainer(dockerContext, dockerContext.getNetworkingConfiguration().getDnsConfiguration().getServiceName()), layerIndex);
  }

}

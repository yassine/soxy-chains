package com.github.yassine.soxychains.subsystem.docker.networking;


import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.yassine.soxychains.subsystem.docker.client.Docker;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Maybe;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC) @Singleton
public class DnsHelper {

  private final DockerConfiguration dockerConfiguration;

  public Maybe<String> getAddress(Docker docker){
    return docker.findContainer(nameSpaceContainer(dockerConfiguration, dockerConfiguration.getNetworkingConfiguration().getDnsConfiguration().getServiceName()))
      .flatMap(container ->
        ofNullable(container.getNetworkSettings())
          .map(ContainerNetworkSettings::getNetworks)
          .map(networks -> networks.get(nameSpaceNetwork(dockerConfiguration, dockerConfiguration.getNetworkingConfiguration().getNetworkName())).getIpAddress())
          .map(Maybe::just)
          .orElse(Maybe.empty())
      );
  }

  public Maybe<String> getAddressAtLayer(Docker docker, Integer layerIndex){
    return docker.findContainer(nameSpaceContainer(dockerConfiguration, dockerConfiguration.getNetworkingConfiguration().getDnsConfiguration().getServiceName()))
      .flatMap(container ->
        ofNullable(container.getNetworkSettings())
          .map(ContainerNetworkSettings::getNetworks)
          .map(networks -> networks.get(nameSpaceLayerNetwork(dockerConfiguration, layerIndex)).getIpAddress())
          .map(Maybe::just)
          .orElse(Maybe.empty())
      );
  }

}

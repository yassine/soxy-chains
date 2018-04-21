package com.github.yassine.soxychains.subsystem.docker.networking;

import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.LayerObserver;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static io.reactivex.Observable.fromIterable;

@AutoService(LayerObserver.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DNSLayerObserver implements LayerObserver {

  private final DockerProvider dockerProvider;
  private final DNSConfiguration dnsConfiguration;
  private final DockerConfiguration dockerConfiguration;

  @Override
  public Maybe<Boolean> onLayerAdd(Integer index, AbstractLayerConfiguration layerConfiguration, Network network) {
    return fromIterable(dockerProvider.dockers()).flatMapMaybe(docker ->
        docker.findContainer(nameSpaceContainer(dockerConfiguration, dnsConfiguration.getServiceName()))
          .flatMap(container -> docker.joinNetwork(container, network.getName()))
    ).reduce(true, AND_OPERATOR)
    .toMaybe();
  }

  @Override
  public Maybe<Boolean> onLayerPreRemove(Integer index, AbstractLayerConfiguration layerConfiguration, Network network) {
    return fromIterable(dockerProvider.dockers()).flatMapSingle(docker ->
      docker.findContainer(nameSpaceContainer(dockerConfiguration, dnsConfiguration.getServiceName()))
        .flatMap(container -> docker.leaveNetwork(container, network.getName()))
        .toSingle(false)
    ).reduce(true, AND_OPERATOR)
    .toMaybe();
  }

}

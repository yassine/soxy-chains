package com.github.yassine.soxychains.subsystem.service;

import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerContext;
import com.github.yassine.soxychains.subsystem.layer.LayerObserver;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.Maybe;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static io.reactivex.Observable.fromIterable;

@AutoService(LayerObserver.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ServicesLayerObserver implements LayerObserver {

  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;
  private final Set<ServicesPlugin> servicesPlugins;
  private final Injector injector;

  @Override
  public Maybe<Boolean> onLayerAdd(Integer index, AbstractLayerContext layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .flatMap(docker -> fromIterable(servicesPlugins).map(this::configOf).flatMapMaybe(
        serviceConfiguration -> docker.findContainer(nameSpaceContainer(dockerContext, serviceConfiguration.serviceName()))
          .flatMap(container -> docker.joinNetwork(container, network.getName()))
      )
    ).reduce(true, AND_OPERATOR)
    .toMaybe();
  }

  @Override
  public Maybe<Boolean> onLayerPreRemove(Integer index, AbstractLayerContext layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .flatMap(docker -> fromIterable(servicesPlugins).map(this::configOf).flatMapMaybe(
        serviceConfiguration -> docker.findContainer(nameSpaceContainer(dockerContext, serviceConfiguration.serviceName()))
          .flatMap(container -> docker.leaveNetwork(container, network.getName()))
        )
      ).reduce(true, AND_OPERATOR)
      .toMaybe();
  }

  @SuppressWarnings("unchecked")
  private ServicesPluginConfiguration configOf(ServicesPlugin plugin){
    return (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) plugin.getClass()));
  }
}

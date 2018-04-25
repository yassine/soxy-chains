package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerContext;
import com.github.yassine.soxychains.subsystem.layer.LayerObserver;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;

@AutoService(LayerObserver.class) @Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class GobetweenLayerObserver implements LayerObserver {

  private final GobetweenProvider gobetweenProvider;
  private final DockerProvider dockerProvider;

  @Override
  public Maybe<Boolean> onLayerAdd(Integer index, AbstractLayerContext layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .flatMapSingle(docker ->
        gobetweenProvider.get(docker.hostConfiguration()).register(index, layerConfiguration)
      ).reduce(true, AND_OPERATOR).toMaybe();
  }

  @Override
  public Maybe<Boolean> onLayerPreRemove(Integer index, AbstractLayerContext layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .flatMapSingle(docker ->
        gobetweenProvider.get(docker.hostConfiguration()).unRegister(index, layerConfiguration)
      ).reduce(true, AND_OPERATOR).toMaybe();
  }
}

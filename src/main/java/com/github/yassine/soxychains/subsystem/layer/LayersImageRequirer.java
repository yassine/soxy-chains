package com.github.yassine.soxychains.subsystem.layer;

import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Map;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC) @AutoService(ImageRequirer.class)
public class LayersImageRequirer implements ImageRequirer{

  private final SoxyChainsConfiguration configuration;
  private final Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> mapLayerConfiguration;

  @Override
  public Observable<DockerImage> require() {
    return Observable.fromIterable(configuration.getLayers())
      .map(layerConfiguration -> mapLayerConfiguration.get(layerConfiguration.getClass()).image(layerConfiguration))
      //remove duplicates
      .collectInto(new HashSet<DockerImage>(), HashSet::add)
      .toObservable()
      .flatMap(Observable::fromIterable);
  }

}

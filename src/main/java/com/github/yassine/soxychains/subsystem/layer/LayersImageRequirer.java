package com.github.yassine.soxychains.subsystem.layer;

import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC) @AutoService(ImageRequirer.class)
public class LayersImageRequirer implements ImageRequirer{

  private final SoxyChainsConfiguration configuration;
  private final Map<Class<? extends AbstractLayerConfiguration>, LayerService> mapLayerConfiguration;

  @Override
  public Observable<DockerImage> require() {
    return Observable.fromIterable(configuration.getLayers())
      .filter(layerConfiguration -> mapLayerConfiguration.get(layerConfiguration.getClass()).getClass().isAnnotationPresent(RequiresImage.class))
      .map(layerConfiguration -> {
        LayerService service = mapLayerConfiguration.get(layerConfiguration.getClass());
        RequiresImage requiresImage = service.getClass().getAnnotation(RequiresImage.class);
        return new DockerImage(requiresImage.name(), URI.create(requiresImage.resourceRoot()), ImmutableMap.of("config", layerConfiguration));
      })
      //remove duplicates
      .collectInto(new HashSet<DockerImage>(), HashSet::add)
      .toObservable()
      .flatMap(Observable::fromIterable);
  }

}

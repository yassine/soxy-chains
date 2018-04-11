package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Set;

import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf;

@AutoService(ImageRequirer.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ServiceImageRequirer implements ImageRequirer{

  private final Set<ServicesPlugin> plugins;
  private final Injector injector;

  @SuppressWarnings("unchecked")
  @Override
  public Observable<DockerImage> require() {
    return Observable.fromIterable(plugins)
      .map(ServicesPlugin::getClass)
      .filter(clazz -> clazz.isAnnotationPresent(RequiresImage.class))
      .map(pluginClass -> new DockerImage(
        pluginClass.getAnnotation(RequiresImage.class).name(),
        URI.create(pluginClass.getAnnotation(RequiresImage.class).resourceRoot()),
        ImmutableMap.of("config", injector.getInstance(configClassOf((Class) pluginClass)))));
  }
}

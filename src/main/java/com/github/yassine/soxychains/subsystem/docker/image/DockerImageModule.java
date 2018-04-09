package com.github.yassine.soxychains.subsystem.docker.image;

import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.github.yassine.soxychains.subsystem.docker.image.resolver.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;

public class DockerImageModule extends AbstractModule{
  @Override
  protected void configure() {
    bind(DockerImageResourceResolver.class)
      .annotatedWith(ClassPath.class)
      .to(ClassPathResolver.class)
      .in(Singleton.class);
    bind(DockerImageResourceResolver.class)
      .annotatedWith(FileSystem.class)
      .to(FileResolver.class)
      .in(Singleton.class);
    bind(DockerImageResolver.class).asEagerSingleton();
    Multibinder<ImageRequirer> multibinder = Multibinder.newSetBinder(binder(), ImageRequirer.class);
    loadSPIClasses(ImageRequirer.class)
      .forEach(clazz -> multibinder.addBinding().to(clazz));
  }
}

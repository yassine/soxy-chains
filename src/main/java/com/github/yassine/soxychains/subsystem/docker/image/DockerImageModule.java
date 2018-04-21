package com.github.yassine.soxychains.subsystem.docker.image;

import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.github.yassine.soxychains.subsystem.docker.image.resolver.DockerImageResolverModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;

public class DockerImageModule extends AbstractModule{
  @Override
  protected void configure() {
    install(new DockerImageResolverModule());
    Multibinder<ImageRequirer> multibinder = Multibinder.newSetBinder(binder(), ImageRequirer.class);
    loadSPIClasses(ImageRequirer.class)
      .forEach(clazz -> multibinder.addBinding().to(clazz));
  }
}

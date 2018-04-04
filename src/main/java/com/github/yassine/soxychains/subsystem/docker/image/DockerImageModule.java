package com.github.yassine.soxychains.subsystem.docker.image;

import com.github.yassine.soxychains.subsystem.docker.image.resolver.*;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

public class DockerImageModule extends PrivateModule{
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

    expose(DockerImageResolver.class);
  }
}

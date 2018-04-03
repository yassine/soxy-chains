package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.artifacts.guice.templating.TemplatingModule;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProviderSupport;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.DockerImageModule;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DockerModule extends PrivateModule{

  @Override
  protected void configure() {
    bind(DockerProvider.class).to(DockerProviderSupport.class);
    expose(DockerProvider.class);
    install(new DockerImageModule());
    install(new TemplatingModule());
  }

  @Provides @Singleton @Exposed
  DockerConfiguration configuration(SoxyChainsConfiguration configuration){
    return configuration.getDocker();
  }
}

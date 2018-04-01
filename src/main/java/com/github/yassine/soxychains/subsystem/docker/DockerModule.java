package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProviderSupport;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.inject.*;

public class DockerModule extends PrivateModule{

  @Override
  protected void configure() {
    bind(DockerProvider.class).to(DockerProviderSupport.class);
    expose(DockerProvider.class);
  }

  @Provides @Singleton @Exposed
  DockerConfiguration configuration(SoxyChainsConfiguration configuration){
    return configuration.getDocker();
  }
}

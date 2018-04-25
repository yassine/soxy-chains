package com.github.yassine.soxychains;

import com.github.yassine.artifacts.guice.scheduling.TaskSchedulerModule;
import com.github.yassine.soxychains.core.CoreModule;
import com.github.yassine.soxychains.subsystem.docker.DockerModule;
import com.github.yassine.soxychains.subsystem.layer.LayerModule;
import com.github.yassine.soxychains.subsystem.service.ServicesModule;
import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SoxyChainsModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new DockerModule());
    install(new ServicesModule());
    install(new CoreModule());
    install(new TaskSchedulerModule());
    install(new LayerModule());
    requireBinding(SoxyChainsContext.class);
  }

}

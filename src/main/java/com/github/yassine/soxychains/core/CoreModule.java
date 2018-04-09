package com.github.yassine.soxychains.core;

import com.google.inject.PrivateModule;

public class CoreModule extends PrivateModule{
  @Override
  protected void configure() {
    bind(TaskLoader.class).to(TaskLoaderSupport.class);
    bind(TaskScheduleProvider.class).to(TaskScheduleProviderSupport.class);
    bind(PhaseRunner.class).to(PhaseRunnerSupport.class);
    expose(TaskScheduleProvider.class);
    expose(PhaseRunner.class);
  }
}

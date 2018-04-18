package com.github.yassine.soxychains.core;

import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.google.inject.PrivateModule;
import com.machinezoo.noexception.Exceptions;

public class CoreModule extends PrivateModule{
  @Override
  protected void configure() {
    bind(TaskLoader.class).to(TaskLoaderSupport.class);
    bind(TaskScheduleProvider.class).to(TaskScheduleProviderSupport.class);
    bind(PhaseRunner.class).to(PhaseRunnerSupport.class);
    expose(TaskScheduleProvider.class);
    expose(PhaseRunner.class);
    GuiceUtils.loadSPIClasses(SoxyChainsThirdPartyModule.class).stream()
      .map(clazz -> Exceptions.sneak().get(clazz::newInstance))
      .forEach(this::install);
  }
}

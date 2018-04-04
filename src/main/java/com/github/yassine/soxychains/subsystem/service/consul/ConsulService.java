package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@AutoService(ServicesPlugin.class) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
@Accessors(fluent = true)
public class ConsulService implements ServicesPlugin<ConsulConfiguration>{
  @Getter
  private final ConsulConfiguration configuration;
}

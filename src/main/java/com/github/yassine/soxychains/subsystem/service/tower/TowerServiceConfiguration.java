package com.github.yassine.soxychains.subsystem.service.tower;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;

import java.util.Collections;
import java.util.List;

public class TowerServiceConfiguration implements ServicesPluginConfiguration {

  private static final String NAME = "tower";
  @Override
  public String serviceName() {
    return NAME;
  }

  @Override
  public String imageName() {
    return NAME;
  }

  @Override
  public List<Integer> servicePorts() {
    return Collections.emptyList();
  }
}

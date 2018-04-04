package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.DefaultPluginSetConfiguration;
import com.github.yassine.soxychains.plugin.Plugin;

import java.util.Map;

public class ServicesConfiguration extends DefaultPluginSetConfiguration<ServicesPluginConfiguration> {

  public ServicesConfiguration(Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration> delegate) {
    super(delegate);
  }

}

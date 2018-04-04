package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.DefaultPluginSetConfiguration;
import com.github.yassine.soxychains.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicesConfiguration extends DefaultPluginSetConfiguration<ServicesPluginConfiguration> {

  public ServicesConfiguration(Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration> delegate) {
    super(delegate);
  }

  List<ServicesPluginConfiguration> configurations(){
    return getDelegate().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

}

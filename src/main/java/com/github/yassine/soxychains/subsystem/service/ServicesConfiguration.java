package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.DefaultPluginSetConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicesConfiguration extends DefaultPluginSetConfiguration<ServicesPluginConfiguration> {

  public ServicesConfiguration(Map<String, ServicesPluginConfiguration> delegate) {
    super(delegate);
  }

  List<ServicesPluginConfiguration> configurations(){
    return getDelegate().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

}

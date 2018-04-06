package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.PluginConfiguration;

import java.util.List;

public interface ServicesPluginConfiguration extends PluginConfiguration {
  String serviceName();
  String imageName();
  List<Integer> servicePorts();
}

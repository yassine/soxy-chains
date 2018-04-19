package com.github.yassine.soxychains.subsystem.service.consul;

import com.google.common.base.Joiner;

public class ConsulNamingUtils {

  private static final String SEPARATOR = "__";

  private ConsulNamingUtils(){}

  public static String namespaceLayerService(Integer layerIndex, ServiceScope scope){
    return Joiner.on(SEPARATOR).join("layer", layerIndex, scope.toString().toLowerCase());
  }

}

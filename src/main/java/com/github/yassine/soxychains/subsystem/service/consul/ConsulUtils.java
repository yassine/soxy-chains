package com.github.yassine.soxychains.subsystem.service.consul;

import com.google.common.base.Joiner;

public class ConsulUtils {

  private static final String SEPARATOR = "__";

  private ConsulUtils(){}

  public static String namespaceLayerService(Integer layerIndex, ServiceScope scope){
    return Joiner.on(SEPARATOR).join("layer", layerIndex, scope.toString().toLowerCase());
  }

  public static int portShift(int layerIndex, int port){
    return port + layerIndex * 2;
  }
}

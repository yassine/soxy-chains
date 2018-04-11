package com.github.yassine.soxychains.subsystem.layer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.io.Serializable;

@Getter
@JsonTypeInfo(
  use      = JsonTypeInfo.Id.NAME,
  include  = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
public abstract class AbstractLayerConfiguration implements Serializable{
  protected int maxNodes    = 50;
  protected int clusterServicePort = 8080;
  protected int localServicePort   = 8081;
  protected double readyRatio = 0.5;
  protected String healthCheckInterval = "45s";
  protected String healthCheckTimeout  = "120s";
}

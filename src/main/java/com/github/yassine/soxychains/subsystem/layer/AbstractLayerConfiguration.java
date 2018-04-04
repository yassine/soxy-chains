package com.github.yassine.soxychains.subsystem.layer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@JsonTypeInfo(
  use      = JsonTypeInfo.Id.NAME,
  include  = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
public abstract class AbstractLayerConfiguration implements Serializable{
  protected int portOffset  = 2;
  protected int maxNodes    = 50;
  protected int clusterServicePort = 8080;
  protected int localServicePort   = 8081;
  protected double readyRatio = 0.5;
  private String healthCheckInterval = "45s";
  private String healthCheckTimeout  = "120s";
  @NotNull
  protected DockerImage image;
}

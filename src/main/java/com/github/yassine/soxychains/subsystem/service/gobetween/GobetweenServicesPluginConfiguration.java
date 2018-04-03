package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class GobetweenServicesPluginConfiguration implements ServicesPluginConfiguration {
  @NotNull
  private String  image       = "gobetween";
  @NotNull
  private String  serviceName = "gobetween";
  @NotNull
  private Integer apiPort        = 7070;
  @NotNull
  private Integer managementPort = 7071;
  @NotNull
  private Integer servicePort    = 8080;
  @NotNull
  private Integer syncPort       = 9080;

  @Override
  public String imageName() {
    return image;
  }
}

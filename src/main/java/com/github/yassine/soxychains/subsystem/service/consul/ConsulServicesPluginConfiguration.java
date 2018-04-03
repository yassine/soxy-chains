package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ConsulServicesPluginConfiguration implements ServicesPluginConfiguration {
  @NotNull
  private String image = "consul";
  @NotNull
  private String  serviceName    = "consul";
  @NotNull
  private Integer servicePort    = 7090;
  @NotNull
  private Integer managementPort = 7091;

  @Override
  public String imageName() {
    return image;
  }
}

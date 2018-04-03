package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DnsServicesPluginConfiguration implements ServicesPluginConfiguration {
  @NotNull
  private String image = "dns-server";
  @NotNull
  private String  serviceName    = "dns-server";
  @NotNull
  private Integer servicePort    = 53;
  @NotNull
  private Integer managementPort = 8070;

  @Override
  public String imageName() {
    return image;
  }
}

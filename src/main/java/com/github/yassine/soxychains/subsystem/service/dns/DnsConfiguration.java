package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DnsConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "dns_server";
  @NotNull
  private String image = "dns_server";
  @NotNull
  private String  serviceName    = "dns_server";
  @NotNull
  private Integer servicePort    = 53;
  @NotNull
  private Integer managementPort = 8070;

  @Override
  public String imageName() {
    return image;
  }
}

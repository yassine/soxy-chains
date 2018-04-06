package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Accessors(fluent = true)
public class DnsConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "dns_server";
  @NotNull
  private String image = "dns_server";
  @NotNull @Getter
  private String  serviceName    = "dns_server";
  @NotNull
  private Integer servicePort    = 53;
  @NotNull
  private Integer managementPort = 8070;

  @Override
  public String imageName() {
    return image;
  }
  @Override
  public List<Integer> servicePorts() {
    return ImmutableList.of(servicePort);
  }
}

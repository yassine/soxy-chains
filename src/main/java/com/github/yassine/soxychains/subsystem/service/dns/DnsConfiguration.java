package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class DnsConfiguration implements ServicesPluginConfiguration {
  public static final String DNS_CONFIG_ID = "dns";
  @NotNull
  private String image = DNS_CONFIG_ID;
  @NotNull
  private String  serviceName    = DNS_CONFIG_ID;
  @NotNull
  private Integer servicePort    = 5353;
  @NotNull
  private Integer managementPort = 8070;

  @Override
  public String serviceName() {
    return DNS_CONFIG_ID;
  }
  @Override
  public String imageName() {
    return DNS_CONFIG_ID;
  }
  @Override
  public List<Integer> servicePorts() {
    return ImmutableList.of(servicePort);
  }
}

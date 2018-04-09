package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class DnsConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "dns";
  @NotNull
  private String image = ID;
  @NotNull
  private String  serviceName    = ID;
  @NotNull
  private Integer servicePort    = 53;
  @NotNull
  private Integer managementPort = 8070;

  @Override
  public String serviceName() {
    return serviceName;
  }

  @Override
  public String imageName() {
    return image;
  }
  @Override
  public List<Integer> servicePorts() {
    return ImmutableList.of(servicePort);
  }
}

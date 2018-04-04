package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.ID;

@Getter @ConfigKey(ID)
public class ConsulConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "consul";
  @NotNull
  private String image = ID;
  @NotNull
  private String  serviceName    = ID;
  @NotNull
  private Integer servicePort    = 7090;
  @NotNull
  private Integer managementPort = 7091;

  @Override
  public String imageName() {
    return image;
  }
}

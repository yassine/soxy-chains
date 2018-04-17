package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.CONSUL_CONFIG_ID;

@SuppressWarnings("FieldCanBeLocal")
@ConfigKey(CONSUL_CONFIG_ID)@Getter
public class ConsulConfiguration implements ServicesPluginConfiguration {
  public static final String CONSUL_CONFIG_ID = "consul";
  @NotNull
  private String image = CONSUL_CONFIG_ID;
  @NotNull
  private String  serviceName    = CONSUL_CONFIG_ID;
  @NotNull
  private Integer servicePort    = 7090;
  @NotNull
  private Integer managementPort = 7091;

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

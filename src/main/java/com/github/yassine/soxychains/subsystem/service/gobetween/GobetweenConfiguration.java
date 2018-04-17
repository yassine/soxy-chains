package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration.GOBETWEEN_CONFIG_ID;

@Getter @ConfigKey(GOBETWEEN_CONFIG_ID)
public class GobetweenConfiguration implements ServicesPluginConfiguration {
  public static final String GOBETWEEN_CONFIG_ID = "gobetween";
  @NotNull
  private String  image       = GOBETWEEN_CONFIG_ID;
  @NotNull
  private String  serviceName = GOBETWEEN_CONFIG_ID;
  @NotNull
  private Integer apiPort        = 7070;
  @NotNull
  private Integer managementPort = 7071;
  @NotNull
  private Integer servicePort    = 8080;
  @NotNull
  private Integer syncPort       = 9080;

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
    return ImmutableList.of(servicePort, apiPort, syncPort);
  }
}

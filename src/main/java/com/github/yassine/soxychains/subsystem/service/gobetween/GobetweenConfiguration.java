package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

import static com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration.ID;

@Getter @ConfigKey(ID) @Accessors(fluent = true)
public class GobetweenConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "gobetween";
  @NotNull
  private String  image       = ID;
  @NotNull @Getter
  private String  serviceName = ID;
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

  @Override
  public List<Integer> servicePorts() {
    return ImmutableList.of(servicePort, apiPort, syncPort);
  }
}

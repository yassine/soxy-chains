package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import lombok.Getter;

import javax.validation.constraints.NotNull;

import static com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration.ID;

@Getter  @ConfigKey(ID)
public class GobetweenConfiguration implements ServicesPluginConfiguration {
  public static final String ID = "gobetween";
  @NotNull
  private String  image       = ID;
  @NotNull
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
}

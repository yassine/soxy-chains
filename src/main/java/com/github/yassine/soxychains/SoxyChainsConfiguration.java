package com.github.yassine.soxychains;

import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SoxyChainsConfiguration {
  @Valid @NotNull
  private DockerConfiguration docker = new DockerConfiguration();
  private ServicesConfiguration services;
  private List<AbstractLayerConfiguration> layers = new ArrayList<>();
}

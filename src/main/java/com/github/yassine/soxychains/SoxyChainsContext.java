package com.github.yassine.soxychains;

import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.github.yassine.soxychains.web.WebAPIConfiguration;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SoxyChainsContext {
  @Valid @NotNull
  private DockerContext docker = new DockerContext();
  @Valid @NotNull
  private List<AbstractLayerConfiguration> layers = new ArrayList<>();
  @Valid @NotNull
  private WebAPIConfiguration api = new WebAPIConfiguration();
}

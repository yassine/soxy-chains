package com.github.yassine.soxychains;

import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
public class SoxyChainsConfiguration {
  @Valid @NotNull
  private DockerConfiguration docker;
}

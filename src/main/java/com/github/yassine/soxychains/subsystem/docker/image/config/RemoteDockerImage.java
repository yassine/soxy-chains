package com.github.yassine.soxychains.subsystem.docker.image.config;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class RemoteDockerImage extends DockerImage {
  public RemoteDockerImage(@NotNull String name) {
    super(name);
  }
}

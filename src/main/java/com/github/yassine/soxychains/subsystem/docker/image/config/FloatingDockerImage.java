package com.github.yassine.soxychains.subsystem.docker.image.config;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.net.URI;

@Getter
public class FloatingDockerImage extends DockerImage {
  @NotNull
  protected final URI root;
  public FloatingDockerImage(@NotNull String name, @NotNull URI root) {
    super(name);
    this.root = root;
  }
}

package com.github.yassine.soxychains.subsystem.docker.image.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Map;

@Getter @EqualsAndHashCode(of = "name") @RequiredArgsConstructor
public class DockerImage {
  @NotNull
  protected final String name;
  @NotNull
  protected final URI root;
  protected final Map<String, Object> templateVars;
}

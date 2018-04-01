package com.github.yassine.soxychains.subsystem.docker.config;

import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DockerConfiguration {
  @NotNull
  private String namespace = "soxy-chains";
  @Valid @NotNull
  private List<DockerHostConfiguration> hosts = new ArrayList<>();
}

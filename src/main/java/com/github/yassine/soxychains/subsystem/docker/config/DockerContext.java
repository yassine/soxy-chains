package com.github.yassine.soxychains.subsystem.docker.config;

import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DockerContext {
  @NotNull
  private String namespace = "soxy-chains";
  private ServicesConfiguration services;
  @Valid @NotNull
  private List<DockerHostConfiguration> hosts = new ArrayList<>();
  @Valid @NotNull
  private NetworkingConfiguration networkingConfiguration = new NetworkingConfiguration();
}

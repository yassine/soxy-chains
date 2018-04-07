package com.github.yassine.soxychains.subsystem.docker.networking;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class NetworkingConfiguration {
  @NotNull
  private String networkName = "soxy-network";
}

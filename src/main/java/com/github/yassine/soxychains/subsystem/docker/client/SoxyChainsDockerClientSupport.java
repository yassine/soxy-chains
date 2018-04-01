package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

@RequiredArgsConstructor @Accessors(fluent = true)
class SoxyChainsDockerClientSupport implements SoxyChainsDockerClient {
  @Delegate(types = DockerClient.class)
  private final DockerClient dockerClient;
  @Getter
  private final DockerHostConfiguration configuration;
}

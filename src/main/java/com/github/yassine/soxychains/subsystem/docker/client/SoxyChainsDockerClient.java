package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;

public interface SoxyChainsDockerClient extends DockerClient {
  DockerHostConfiguration configuration();
}

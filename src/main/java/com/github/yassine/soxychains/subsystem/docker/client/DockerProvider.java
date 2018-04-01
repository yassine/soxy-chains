package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;

public interface DockerProvider {
  Docker get(DockerHostConfiguration configuration);
  SoxyChainsDockerClient getClient(DockerHostConfiguration configuration);
}

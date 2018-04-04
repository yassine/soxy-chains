package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;

import java.util.Set;

public interface DockerProvider {
  Docker get(DockerHostConfiguration configuration);
  SoxyChainsDockerClient getClient(DockerHostConfiguration configuration);
  Set<Docker> dockers();
  Set<SoxyChainsDockerClient> clients();
}

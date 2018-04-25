package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import io.reactivex.Observable;

public interface DockerProvider {
  Docker get(DockerHostConfiguration configuration);
  SoxyChainsDockerClient getClient(DockerHostConfiguration configuration);
  Observable<Docker> dockers();
  Observable<SoxyChainsDockerClient> clients();
}

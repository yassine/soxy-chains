package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import io.reactivex.Observable;

public interface HostManager {
  Observable<DockerHostConfiguration> list();
}

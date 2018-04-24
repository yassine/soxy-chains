package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface HostManager {
  Single<Boolean> add(DockerHostConfiguration dockerHostConfiguration);
  Single<Boolean> remove(DockerHostConfiguration dockerHostConfiguration);
  Observable<DockerHostConfiguration> list();
}

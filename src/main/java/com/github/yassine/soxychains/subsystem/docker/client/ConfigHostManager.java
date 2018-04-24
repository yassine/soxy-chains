package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ConfigHostManager implements HostManager {

  private final List<DockerHostConfiguration> configurations;

  @Inject
  private DockerProvider dockerProvider;

  @Override
  public Single<Boolean> add(DockerHostConfiguration dockerHostConfiguration) {
    return null;
  }

  @Override
  public Single<Boolean> remove(DockerHostConfiguration dockerHostConfiguration) {
    return null;
  }

  @Override
  public Observable<DockerHostConfiguration> list() {

    return null;
  }
}

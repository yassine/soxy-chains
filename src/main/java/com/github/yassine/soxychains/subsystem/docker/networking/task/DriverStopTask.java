package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.SOXY_DRIVER_NAME;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static io.reactivex.Observable.fromIterable;

@AutoService(Task.class)
@RunOn(Phase.STOP) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DriverStopTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;

  @Override
  public Single<Boolean> execute() {
    return fromIterable(dockerProvider.dockers())
      .flatMapMaybe(docker -> docker.stopContainer(nameSpaceContainer(dockerConfiguration, SOXY_DRIVER_NAME))
                                .map(c -> true)
                                .defaultIfEmpty(true))
      .reduce(true, (a,b) -> a && b);
  }

}

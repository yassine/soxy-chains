package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.ReverseDependsOn;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceNetwork;
import static io.reactivex.Observable.concat;
import static io.reactivex.Observable.fromIterable;

@RunOn(Phase.STOP)
@AutoService(Task.class) @ReverseDependsOn(DriverStopTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingStopTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final NetworkingConfiguration networkingConfiguration;

  @Override
  public Single<Boolean> execute() {
    return concat(
      fromIterable(dockerProvider.dockers())
        .flatMapMaybe(docker -> docker.stopContainer(
          nameSpaceContainer(dockerConfiguration, networkingConfiguration.getDnsConfiguration().getServiceName()))
        ).reduce(true, AND_OPERATOR).toObservable(),
      fromIterable(dockerProvider.dockers())
        .flatMapMaybe(docker -> docker.removeNetwork(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName())).defaultIfEmpty(false))
        .subscribeOn(Schedulers.io())
    ).reduce(true, AND_OPERATOR);
  }

}

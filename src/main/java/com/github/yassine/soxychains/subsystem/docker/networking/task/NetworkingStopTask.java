package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.ReverseDependsOn;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
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

@RunOn(Phase.STOP)
@AutoService(Task.class) @ReverseDependsOn(DriverStopTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingStopTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;
  private final NetworkingConfiguration networkingConfiguration;

  @Override
  public Single<Boolean> execute() {
    return concat(
      dockerProvider.dockers()
        .flatMapMaybe(docker -> docker.stopContainer(
          nameSpaceContainer(dockerContext, networkingConfiguration.getDnsConfiguration().getServiceName()))
        ).reduce(true, AND_OPERATOR).toObservable(),
      dockerProvider.dockers()
        .flatMapMaybe(docker -> docker.removeNetwork(nameSpaceNetwork(dockerContext, networkingConfiguration.getNetworkName())).defaultIfEmpty(false))
        .subscribeOn(Schedulers.io())
    ).reduce(true, AND_OPERATOR);
  }

}

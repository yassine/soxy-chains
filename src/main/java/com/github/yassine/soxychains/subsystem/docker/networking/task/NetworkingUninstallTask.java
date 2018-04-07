package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.task.ImageUninstallTask;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceNetwork;

@RunOn(Phase.UNINSTALL)
@DependsOn(ImageUninstallTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingUninstallTask implements Task{
  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final NetworkingConfiguration networkingConfiguration;
  @Override
  public Single<Boolean> execute() {
    return Observable.fromIterable(dockerProvider.dockers())
      .flatMapMaybe(docker -> docker.removeNetwork(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()),
                                removeNetworkCmd -> {},
                                name -> {}
                              ).defaultIfEmpty(false))
      .reduce(true, (a, b) -> a && b);
  }
}

package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.ReverseDependsOn;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceLayerNetwork;
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
  private final SoxyChainsConfiguration soxyChainsConfiguration;

  @Override
  public Single<Boolean> execute() {
    return concat(
      fromIterable(soxyChainsConfiguration.getLayers())
        .flatMap(layerConfiguration ->
          fromIterable(dockerProvider.dockers())
            .flatMapMaybe(docker -> docker.removeNetwork(nameSpaceLayerNetwork(dockerConfiguration, soxyChainsConfiguration.getLayers().indexOf(layerConfiguration)),
              (createNetworkCmd) -> {},
              (networkID) -> {})
            )
        ),
      fromIterable(dockerProvider.dockers())
        .flatMapMaybe(docker -> docker.removeNetwork(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()),
          removeNetworkCmd -> {},
          name -> {}).defaultIfEmpty(false))
    ).reduce(true, (a, b) -> a && b);
  }

}

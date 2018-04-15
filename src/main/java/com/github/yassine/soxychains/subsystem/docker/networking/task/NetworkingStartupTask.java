package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromIterable;

@RunOn(Phase.START)
@AutoService(Task.class) @DependsOn(DriverStartupTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingStartupTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final NetworkingConfiguration networkingConfiguration;
  private final SoxyChainsConfiguration soxyChainsConfiguration;

  @Override @SneakyThrows
  public Single<Boolean> execute() {
    Thread.sleep(5000L);
    return Observable.concat(
      fromIterable(soxyChainsConfiguration.getLayers())
        .flatMap(layerConfiguration ->
          fromIterable(dockerProvider.dockers())
            .flatMapMaybe(docker -> docker.createNetwork(nameSpaceLayerNetwork(dockerConfiguration, soxyChainsConfiguration.getLayers().indexOf(layerConfiguration)),
              (createNetworkCmd) -> {
                createNetworkCmd.withDriver(soxyDriverName(dockerConfiguration));
              },
              (networkID) -> {})
            )
        ).map(StringUtils::isNotEmpty),
      fromIterable(dockerProvider.dockers())
        .flatMapMaybe(docker -> docker.createNetwork(
          nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()),
          createNetworkCmd -> {
            createNetworkCmd.withDriver(soxyDriverName(dockerConfiguration));
          },
          name -> {}
        ).map(StringUtils::isNotEmpty).defaultIfEmpty(false))
    ).reduce(true, (a, b) -> a && b);
  }

}

package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
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
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromIterable;

@RunOn(Phase.START)
@AutoService(Task.class) @DependsOn(DriverStartupTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingStartupTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final NetworkingConfiguration networkingConfiguration;

  @Override @SneakyThrows
  public Single<Boolean> execute() {
    return fromIterable(dockerProvider.dockers())
      .flatMapMaybe(docker -> docker.createNetwork(
        nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()),
        createNetworkCmd -> createNetworkCmd.withDriver(soxyDriverName(dockerConfiguration))
      ).subscribeOn(Schedulers.single()).map(StringUtils::isNotEmpty).defaultIfEmpty(false))
    .reduce(true, AND_OPERATOR)
    //start the platform DNS server
    .flatMap(result ->
      fromIterable(dockerProvider.dockers())
        .flatMapSingle(docker ->
          docker.runContainer(
            nameSpaceContainer(dockerConfiguration, networkingConfiguration.getDnsConfiguration().getServiceName()),
            nameSpaceImage(dockerConfiguration, networkingConfiguration.getDnsConfiguration().getImage()),
            createContainerCmd ->{
              // make the dns server join the main network, so that it is accessible by services
              createContainerCmd.withNetworkMode(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()));
              createContainerCmd.withLabels(labelizeNamedEntity(networkingConfiguration.getDnsConfiguration().getServiceName(), dockerConfiguration));
            }
          ).map(Objects::nonNull)
          .toSingle(false)
        ).reduce(result, AND_OPERATOR)
    );
  }

}

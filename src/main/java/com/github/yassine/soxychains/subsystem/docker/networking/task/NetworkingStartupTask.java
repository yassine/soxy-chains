package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
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
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;

@RunOn(Phase.START)
@AutoService(Task.class) @DependsOn(DriverStartupTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingStartupTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;
  private final NetworkingConfiguration networkingConfiguration;

  @Override @SneakyThrows
  public Single<Boolean> execute() {
    return dockerProvider.dockers()
      .flatMapMaybe(docker -> docker.createNetwork(
        nameSpaceNetwork(dockerContext, networkingConfiguration.getNetworkName()),
        createNetworkCmd -> createNetworkCmd.withDriver(soxyDriverName(dockerContext))
      ).subscribeOn(Schedulers.single()).map(StringUtils::isNotEmpty).defaultIfEmpty(false))
    .reduce(true, AND_OPERATOR)
    //start the platform DNS server
    .flatMap(result ->
      dockerProvider.dockers()
        .flatMapSingle(docker ->
          docker.runContainer(
            nameSpaceContainer(dockerContext, networkingConfiguration.getDnsConfiguration().getServiceName()),
            nameSpaceImage(dockerContext, networkingConfiguration.getDnsConfiguration().getImage()),
            createContainerCmd ->{
              // make the dns server join the main network, so that it is accessible by services
              createContainerCmd.withNetworkMode(nameSpaceNetwork(dockerContext, networkingConfiguration.getNetworkName()));
              createContainerCmd.withLabels(labelizeNamedEntity(networkingConfiguration.getDnsConfiguration().getServiceName(), dockerContext));
            }
          ).map(Objects::nonNull)
          .toSingle(false)
        ).reduce(result, AND_OPERATOR)
    );
  }

}

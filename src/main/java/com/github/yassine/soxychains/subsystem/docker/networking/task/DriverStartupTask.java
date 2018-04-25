package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.dockerjava.api.model.Bind;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;

@AutoService(Task.class)
@RunOn(Phase.START) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DriverStartupTask implements Task {

  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;

  @Override
  public Single<Boolean> execute() {
    return dockerProvider.dockers()
      .flatMapMaybe(docker -> docker.runContainer(nameSpaceContainer(dockerContext, SOXY_DRIVER_NAME), nameSpaceImage(dockerContext, SOXY_DRIVER_NAME),
        createContainerCmd -> createContainerCmd.withNetworkMode("host")
          .withPrivileged(true)
          .withBinds(
            Bind.parse("/var/run/docker.sock:/var/run/docker.sock"),
            Bind.parse("/run/docker/plugins:/run/docker/plugins")
          )
          .withLabels(
            labelizeNamedEntity(SOXY_DRIVER_NAME, dockerContext)
          )
        )
        .map(c -> true)
        .defaultIfEmpty(true))
      .reduce(true, AND_OPERATOR);
  }

}

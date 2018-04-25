package com.github.yassine.soxychains.subsystem.docker.networking.task;

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
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.SOXY_DRIVER_NAME;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;

@AutoService(Task.class) @Slf4j
@RunOn(Phase.STOP) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DriverStopTask implements Task{

  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;

  @Override
  public Single<Boolean> execute() {
    AtomicLong counter = new AtomicLong(0L);
    return dockerProvider.dockers()
      .flatMapMaybe(docker -> docker.stopContainer(
          nameSpaceContainer(dockerContext, SOXY_DRIVER_NAME),
          beforestop -> {
            //leave the driver the necessary time to clear a potentially large number of iptables rules, bridges etc.
            beforestop.withTimeout(600);
            counter.set(System.currentTimeMillis());
          },
          containerID -> log.info("Ended Stopping driver task container after {} seconds", (System.currentTimeMillis() - counter.get())/1000),
          removeContainerCmd -> log.info("Removing driver container..."),
          id -> log.info("Ended network driver removal.")
        ).map(c -> true)
        .defaultIfEmpty(true)
      ).reduce(true, AND_OPERATOR);
  }

}

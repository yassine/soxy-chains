package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.scheduling.ReverseDependsOn;
import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.task.NetworkingStopTask;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer;
import static com.google.common.collect.Lists.reverse;
import static io.reactivex.Observable.fromFuture;
import static io.reactivex.Observable.fromIterable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
@RunOn(Phase.STOP) @AutoService(Task.class) @ReverseDependsOn(NetworkingStopTask.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ServicesStopTask implements Task{

  private final Set<ServicesPlugin> services;
  private final TaskScheduler taskScheduler;
  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final Injector injector;

  @Override @SuppressWarnings("unchecked")
  public Single<Boolean> execute() {
    // actions to come are executed on each host in parallel
    return fromIterable(dockerProvider.dockers())
      //Services are stopped in the reverse order of their startup
      .flatMap(docker -> fromIterable(reverse(taskScheduler.scheduleInstances(services)))
          //for each wave of services
          .flatMap(services -> fromFuture(supplyAsync(() -> fromIterable(services)
              .flatMapMaybe(service ->
                //stop and remove the container that relates to the given service
                docker.stopContainer(nameSpaceContainer(dockerConfiguration, configOf(service).serviceName()),
                  (stopContainer) -> {},
                  (containerID) -> {},
                  (removeContainer) -> {},
                  (container) -> {}
                ).subscribeOn(Schedulers.io()).map(result -> true)
              )
              // reduce the results as a single boolean value
              .defaultIfEmpty(false).reduce(true, (a,b) -> a && b).blockingGet())
            )
          )
      ).subscribeOn(Schedulers.io()).reduce(true, (a,b) -> a && b);
  }

  @SuppressWarnings("unchecked")
  private ServicesPluginConfiguration configOf(ServicesPlugin plugin){
    return (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) plugin.getClass()));
  }
}

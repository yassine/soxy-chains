package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.task.NetworkingStartupTask;
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
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromFuture;
import static io.reactivex.Observable.fromIterable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j @DependsOn(NetworkingStartupTask.class)
@RunOn(Phase.START) @AutoService(Task.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ServicesStartTask implements Task{

  private final Set<ServicesPlugin> services;
  private final TaskScheduler taskScheduler;
  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;
  private final NetworkingConfiguration networkingConfiguration;
  private final Injector injector;

  @Override @SuppressWarnings("unchecked")
  public Single<Boolean> execute() {
    // actions to come are executed on each host in parallel
    return fromIterable(dockerProvider.dockers())
      //Knowing that a service may require other ones to start before actually starting, services will be started
      //in waves of tasks that can be executed in parallel.
      .flatMap(docker -> fromIterable(taskScheduler.scheduleInstances(services))
          //for each wave of services
          .flatMap(services -> fromFuture(supplyAsync(() -> fromIterable(services)
              //for each service
              .flatMapMaybe(service ->
                //create & start the container that relates to the given service
                docker.runContainer(configOf(service).serviceName(), configOf(service).imageName(),
                  // The pre-create container hook is used to allow services configuring the container before its creation
                  (createContainer) -> {
                    service.configureContainer(createContainer, configOf(service), dockerConfiguration);
                    createContainer.withNetworkMode(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()));
                    createContainer.withName(nameSpaceContainer(dockerConfiguration, configOf(service).serviceName()));
                    createContainer.withImage(nameSpaceImage(dockerConfiguration, configOf(service).imageName()));
                    createContainer.withLabels(labelizeNamedEntity(configOf(service).serviceName(), dockerConfiguration));
                  },
                  //The post-create hook is not used
                  (containerID) -> {},
                  //The pre-start hook is not used
                  (startContainer) -> {},
                  //The post-start hook is used
                  (container) -> {}
                ).subscribeOn(Schedulers.io()).map(result -> service)
              )
              // wait for programmatic startup check
              .flatMapSingle(service -> (Single<Boolean>) service.isReady(docker.hostConfiguration(), configOf(service)))
              // reduce the results as a single boolean value
              .defaultIfEmpty(false).reduce(true, (a, b) -> a && b).blockingGet())
            )
          )
      ).subscribeOn(Schedulers.io()).reduce(true, (a,b) -> a && b);
  }

  @SuppressWarnings("unchecked")
  private ServicesPluginConfiguration configOf(ServicesPlugin plugin){
    return (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) plugin.getClass()));
  }
}

package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
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
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceNetwork;
import static io.reactivex.Observable.fromFuture;
import static io.reactivex.Observable.fromIterable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
@RunOn(Phase.START)
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
      //in parallel as a set of waves of startup tasks that can start in parallel.
      .flatMap(docker -> fromIterable(taskScheduler.scheduleInstances(services))
          //for each wave of services/services
          .flatMap(services -> fromFuture(supplyAsync(() -> fromIterable(services)
              .flatMapMaybe(service ->
                //create & start the container that relates to the given service
                docker.startContainer(configOf(service).serviceName(), configOf(service).imageName(),
                  (createContainer) -> {
                    service.configureContainer(createContainer, configOf(service), dockerConfiguration);
                    createContainer.withNetworkMode(nameSpaceNetwork(dockerConfiguration, networkingConfiguration.getNetworkName()));
                    createContainer.withName(nameSpaceContainer(dockerConfiguration, configOf(service).serviceName()));
                  },
                  (containerID) -> {},
                  (startContainer) -> {},
                  (container) -> {
                    service.isReady(docker.hostConfiguration(), configOf(service)).blockingGet();
                  }
                ).subscribeOn(Schedulers.io()).map(result -> service)
              )
              // wait for programmatic startup check
              .flatMapSingle(service -> (Single<Boolean>) service.isReady(docker.hostConfiguration(), configOf(service)))
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

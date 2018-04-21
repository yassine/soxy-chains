package com.github.yassine.soxychains.subsystem.layer.task;

import com.github.yassine.artifacts.guice.scheduling.ReverseDependsOn;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.github.yassine.soxychains.subsystem.layer.LayerService;
import com.github.yassine.soxychains.subsystem.service.ServicesStopTask;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@AutoService(Task.class) @ReverseDependsOn(ServicesStopTask.class) @RunOn(Phase.STOP)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class LayerStopTask implements Task{

  private final DockerProvider dockerProvider;
  private final Set<LayerProvider> layerServices;
  private final DockerConfiguration dockerConfiguration;
  private final SoxyChainsConfiguration soxyChainsConfiguration;
  private final LayerService layerService;

  @Override
  public Single<Boolean> execute() {
    return concat(//remove all the nodes on all layers
              fromIterable(dockerProvider.clients()).subscribeOn(Schedulers.io())
                .flatMap(docker ->
                  fromIterable(layerServices).flatMap(currentLayerService ->
                    fromFuture(supplyAsync(
                      docker.listContainersCmd()
                        .withLabelFilter(
                          filterLayerNode(currentLayerService.getClass(), dockerConfiguration)
                        )::exec))
                      .flatMap(Observable::fromIterable)
                      .map(container -> Pair.of(docker, container)))
                )
                .flatMapMaybe(pair -> dockerProvider.get(pair.getKey().configuration()).stopContainer(
                    namespaceLayerNode(dockerConfiguration, Integer.parseInt(pair.getValue().getLabels().get(LAYER_INDEX)), pair.getValue().getLabels().get(RANDOM_LABEL))
                  )
                ),
              //then remove the layers (implicitly the related networks too)
              fromIterable(soxyChainsConfiguration.getLayers())
                .flatMapSingle(layerConfiguration -> layerService.removeLayer(soxyChainsConfiguration.getLayers().indexOf(layerConfiguration), layerConfiguration)
                  .subscribeOn(Schedulers.io()))
            )
            .reduce(true , AND_OPERATOR) ;
  }

}

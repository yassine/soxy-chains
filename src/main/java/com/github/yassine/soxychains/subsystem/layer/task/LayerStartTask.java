package com.github.yassine.soxychains.subsystem.layer.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.layer.LayerService;
import com.github.yassine.soxychains.subsystem.service.ServicesStartTask;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static io.reactivex.Observable.fromIterable;

@DependsOn(ServicesStartTask.class) @RunOn(Phase.START) @AutoService(Task.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class LayerStartTask implements Task{
  private final SoxyChainsContext soxyChainsContext;
  private final LayerService layerService;

  @Override
  public Single<Boolean> execute() {
    return fromIterable(soxyChainsContext.getLayers())
      .flatMapSingle(layerConfiguration -> layerService.addLayer(soxyChainsContext.getLayers().indexOf(layerConfiguration), layerConfiguration).subscribeOn(Schedulers.io()))
      .reduce(true, AND_OPERATOR);
  }
}

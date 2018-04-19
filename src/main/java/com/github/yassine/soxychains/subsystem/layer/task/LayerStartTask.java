package com.github.yassine.soxychains.subsystem.layer.task;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.service.ServicesStartTask;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@DependsOn(ServicesStartTask.class) @RunOn(Phase.START)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class LayerStartTask implements Task{
  @Override
  public Single<Boolean> execute() {
    return Single.just(true);
  }
}

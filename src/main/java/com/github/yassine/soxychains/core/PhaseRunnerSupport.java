package com.github.yassine.soxychains.core;

import com.google.inject.Inject;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.reactivex.Observable.fromFuture;
import static io.reactivex.Observable.fromIterable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class PhaseRunnerSupport implements PhaseRunner {

  private final TaskScheduleProvider taskScheduleProvider;

  @SneakyThrows
  public Single<Boolean> runPhase(Phase phase) {
    return fromIterable(taskScheduleProvider.get(phase))
      .flatMap(tasksWave -> fromFuture(supplyAsync(() -> fromIterable(tasksWave)
          .flatMapSingle(task ->
            Single.fromFuture(supplyAsync(() -> {
              log.info("Executing task '{}'", task.name());
              Boolean result = task.execute().blockingGet();
              log.info("Successfully Executed task '{}'. Output: {}", task.name(), result);
              return result;
            }))
            .subscribeOn(Schedulers.io())
            .doOnError( exception -> {
              log.error("An error while executing task '{}'", task.name());
              log.error(exception.getMessage(), exception);
            }).subscribeOn(Schedulers.io())
          ).reduce(true, (a, b) -> a && b).blockingGet())))
      .reduce(true, (a, b) -> a && b);
  }

}

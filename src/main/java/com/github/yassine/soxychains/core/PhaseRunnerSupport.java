package com.github.yassine.soxychains.core;

import com.google.inject.Inject;
import io.reactivex.Observable;
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
  public void runPhase(Phase phase) {
    fromIterable(taskScheduleProvider.get(phase))
      .blockingSubscribe(tasks -> fromIterable(tasks)
        .flatMap(task ->
          fromFuture(supplyAsync(() -> {
            log.info("Executing task '{}'", task.name());
            Boolean result = task.execute().blockingGet();
            log.info("Successfully Executed task '{}'. Output: {}", task.name(), result);
            return result;
          }))
          .subscribeOn(Schedulers.io())
          .onErrorResumeNext( (e) -> {
            log.error("An error while executing task '{}'", task.name());
            log.error(e.getMessage(), e);
            return Observable.empty();
          })
        ).blockingSubscribe()
      );
  }

}

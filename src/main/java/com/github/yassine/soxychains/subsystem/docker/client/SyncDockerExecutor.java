package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.command.SyncDockerCmd;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Slf4j @RequiredArgsConstructor
class SyncDockerExecutor<R, C extends SyncDockerCmd<R> > {

  private final C cmd;
  private final DockerHostConfiguration configuration;
  private Function<R, String> successFormatter  = result -> format("Successfully execute command '%s'", SyncDockerExecutor.this.cmd.getClass().getSimpleName());
  private Function<Throwable, String> errorFormatter = exception -> format("Error occurred while executing command '%s' : %s", SyncDockerExecutor.this.cmd.getClass().getSimpleName(), exception.getMessage());
  private Optional<Consumer<C>> beforeExecute = Optional.empty();
  private Optional<Consumer<R>> afterExecute = Optional.empty();

  @SuppressWarnings("unchecked")
  public Maybe<R> execute(){
    Preconditions.checkNotNull(cmd, "command can't be null");
    Preconditions.checkNotNull(configuration);
    return Maybe.fromFuture(supplyAsync(() -> {
      try{
        beforeExecute.ifPresent(before -> before.accept(cmd));
        R result = cmd.exec();
        log.info(successFormatter.apply(result));
        afterExecute.ifPresent(after -> after.accept(result));
        if(result == null){
          return Maybe.<R>empty();
        }
        return Maybe.just(result);
      }catch (Exception e){
        log.error(errorFormatter.apply(e));
        log.error(e.getMessage(), e);
        return Maybe.<R>empty();
      }
    })).flatMap(v -> v).subscribeOn(Schedulers.io());
  }

  public SyncDockerExecutor<R, C> withSuccessFormatter(Function<R, String> successFormatter){
    Preconditions.checkNotNull(successFormatter);
    this.successFormatter = successFormatter;
    return this;
  }

  public SyncDockerExecutor<R, C> withErrorFormatter(Function<Throwable, String> errorFormatter){
    Preconditions.checkNotNull(errorFormatter);
    this.errorFormatter = errorFormatter;
    return this;
  }

  public SyncDockerExecutor<R, C> withBeforeExecute(Consumer<C> beforeExecute){
    this.beforeExecute = Optional.ofNullable(beforeExecute);
    return this;
  }
  public SyncDockerExecutor<R, C> withAfterExecute(Consumer<R> afterExecute){
    this.afterExecute = Optional.ofNullable(afterExecute);
    return this;
  }

}

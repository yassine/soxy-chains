package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.command.SyncDockerCmd;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Slf4j @RequiredArgsConstructor
class SyncDockerExecutor<T, CMD extends SyncDockerCmd<T> > {

  private final CMD cmd;
  private final DockerHostConfiguration configuration;
  private Function<T, String> successFormatter = (t) -> format("Successfully execute command '%s'", SyncDockerExecutor.this.cmd.getClass().getSimpleName());
  private Function<Throwable, String> errorFormatter = (e) -> format("Error occurred while executing command '%s' : %s", SyncDockerExecutor.this.cmd.getClass().getSimpleName(), e.getMessage());
  private Optional<Consumer<CMD>> beforeExecute;
  private Optional<Consumer<T>> afterExecute;

  @SuppressWarnings("unchecked")
  public Maybe<T> execute(){
    Preconditions.checkNotNull(cmd, "command can't be null");
    Preconditions.checkNotNull(configuration);
    return Maybe.fromFuture(supplyAsync(() -> {
      try{
        beforeExecute.ifPresent(before -> before.accept(cmd));
        T result = cmd.exec();
        successFormatter.apply(result);
        afterExecute.ifPresent(after -> after.accept(result));
        if(result == null){
          return (Maybe<T>) Maybe.empty();
        }
        return Maybe.just(result);
      }catch (Exception e){
        log.error(errorFormatter.apply(e));
        log.error(e.getMessage(), e);
        return (Maybe<T>) Maybe.empty();
      }
    })).flatMap(v -> v);
  }

  public SyncDockerExecutor<T, CMD> withSuccessFormatter(Function<T, String> successFormatter){
    Preconditions.checkNotNull(successFormatter);
    this.successFormatter = successFormatter;
    return this;
  }

  public SyncDockerExecutor<T, CMD> withErrorFormatter(Function<Throwable, String> errorFormatter){
    Preconditions.checkNotNull(errorFormatter);
    this.errorFormatter = errorFormatter;
    return this;
  }

  public SyncDockerExecutor<T, CMD> withBeforeExecute(Consumer<CMD> beforeExecute){
    this.beforeExecute = Optional.ofNullable(beforeExecute);
    return this;
  }
  public SyncDockerExecutor<T, CMD> withAfterExecute(Consumer<T> afterExecute){
    this.afterExecute = Optional.ofNullable(afterExecute);
    return this;
  }

}

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
class SyncDockerExecutor<RESULT, CMD extends SyncDockerCmd<RESULT> > {

  private final CMD cmd;
  private final DockerHostConfiguration configuration;
  private Function<RESULT, String> successFormatter  = result -> format("Successfully execute command '%s'", SyncDockerExecutor.this.cmd.getClass().getSimpleName());
  private Function<Throwable, String> errorFormatter = exception -> format("Error occurred while executing command '%s' : %s", SyncDockerExecutor.this.cmd.getClass().getSimpleName(), exception.getMessage());
  private Optional<Consumer<CMD>> beforeExecute = Optional.empty();
  private Optional<Consumer<RESULT>> afterExecute = Optional.empty();

  @SuppressWarnings("unchecked")
  public Maybe<RESULT> execute(){
    Preconditions.checkNotNull(cmd, "command can't be null");
    Preconditions.checkNotNull(configuration);
    return Maybe.fromFuture(supplyAsync(() -> {
      try{
        beforeExecute.ifPresent(before -> before.accept(cmd));
        RESULT result = cmd.exec();
        log.info(successFormatter.apply(result));
        afterExecute.ifPresent(after -> after.accept(result));
        if(result == null){
          return Maybe.<RESULT>empty();
        }
        return Maybe.just(result);
      }catch (Exception e){
        log.error(errorFormatter.apply(e));
        log.error(e.getMessage(), e);
        return Maybe.<RESULT>empty();
      }
    })).flatMap(v -> v).subscribeOn(Schedulers.io());
  }

  public SyncDockerExecutor<RESULT, CMD> withSuccessFormatter(Function<RESULT, String> successFormatter){
    Preconditions.checkNotNull(successFormatter);
    this.successFormatter = successFormatter;
    return this;
  }

  public SyncDockerExecutor<RESULT, CMD> withErrorFormatter(Function<Throwable, String> errorFormatter){
    Preconditions.checkNotNull(errorFormatter);
    this.errorFormatter = errorFormatter;
    return this;
  }

  public SyncDockerExecutor<RESULT, CMD> withBeforeExecute(Consumer<CMD> beforeExecute){
    this.beforeExecute = Optional.ofNullable(beforeExecute);
    return this;
  }
  public SyncDockerExecutor<RESULT, CMD> withAfterExecute(Consumer<RESULT> afterExecute){
    this.afterExecute = Optional.ofNullable(afterExecute);
    return this;
  }

}

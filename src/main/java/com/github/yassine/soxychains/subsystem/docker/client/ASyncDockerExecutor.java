package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") @Slf4j
class ASyncDockerExecutor<CMD_T extends AsyncDockerCmd<CMD_T, A_RES_T>, A_RES_T, CALLBACK extends ResultCallback<A_RES_T>, RESULT> {

  private final CMD_T cmd;
  private final DockerHostConfiguration configuration;
  private CALLBACK callback;
  private Function<CALLBACK, RESULT> resultExtractor;
  private Function<RESULT, String> successFormatter;
  private Function<Throwable, String> errorFormatter;
  private Optional<Consumer<CMD_T>> beforeExecute;
  private Optional<Consumer<RESULT>> afterExecute;

  public ASyncDockerExecutor(CMD_T cmd, DockerHostConfiguration configuration) {
    this.cmd = cmd;
    this.configuration = configuration;
    this.successFormatter = result -> format("Successfully executed command '%s'", cmd.getClass().getSimpleName());
    this.errorFormatter = exception -> format("Error occurred while executing command '%s' : %s", cmd.getClass().getSimpleName(), exception.getMessage());
  }

  public Maybe<RESULT> execute(){

    Preconditions.checkNotNull(cmd);
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(callback);
    Preconditions.checkNotNull(resultExtractor);

    return Maybe.fromFuture(supplyAsync(() -> {
      try{
        beforeExecute.ifPresent(before -> before.accept(cmd));
        cmd.exec(callback);
        RESULT result = getResultExtractor().apply(callback);
        log.info(successFormatter.apply(result));
        afterExecute.ifPresent(after -> after.accept(result));
        return Maybe.just(result);
      }catch (Exception e){
        log.error(e.getMessage(), e);
        log.error(errorFormatter.apply(e));
        return Maybe.<RESULT>empty();
      }
    })).flatMap(v -> v);
  }

  Function<CALLBACK, RESULT> getResultExtractor(){
    return resultExtractor;
  }


  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withSuccessFormatter(Function<RESULT, String> successFormatter){
    Preconditions.checkNotNull(successFormatter);
    this.successFormatter = successFormatter;
    return this;
  }

  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withErrorFormatter(Function<Throwable, String> errorFormatter){
    Preconditions.checkNotNull(errorFormatter);
    this.errorFormatter = errorFormatter;
    return this;
  }

  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withBeforeExecute(Consumer<CMD_T> beforeExecute){
    this.beforeExecute = Optional.ofNullable(beforeExecute);
    return this;
  }

  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withAfterExecute(Consumer<RESULT> afterExecute){
    this.afterExecute = Optional.ofNullable(afterExecute);
    return this;
  }

  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withCallBack(CALLBACK callBack){
    this.callback = callBack;
    return this;
  }

  public ASyncDockerExecutor<CMD_T, A_RES_T, CALLBACK, RESULT> withResultExtractor(Function<CALLBACK, RESULT> resultExtractor){
    this.resultExtractor = resultExtractor;
    return this;
  }

}

package com.github.yassine.soxychains.core;

import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.concurrent.CompletableFuture;

public class FluentUtils {

  private FluentUtils() {}

  public static <VALUE> VALUE runAndGet(Runnable runnable, VALUE returnValue){
    runnable.run();
    return returnValue;
  }

  public static <VALUE> Single<VALUE> runAndGetAsSingle(Runnable runnable, VALUE returnValue){
    return Single.fromFuture(CompletableFuture.supplyAsync(() -> runAndGet(runnable, returnValue)));
  }

  public static <VALUE> Maybe<VALUE> runAndGetAsMaybe(Runnable runnable, VALUE returnValue){
    return Maybe.fromFuture(CompletableFuture.supplyAsync(() -> runAndGet(runnable, returnValue)));
  }

  public static <SELF> SELF identity(SELF self){
    return self;
  }

}

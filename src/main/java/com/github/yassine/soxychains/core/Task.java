package com.github.yassine.soxychains.core;

import io.reactivex.Maybe;

public interface Task {
  Maybe<Boolean> execute();
  default String name(){
    return getClass().getName();
  }
}

package com.github.yassine.soxychains.core;

import io.reactivex.Single;

public interface Task {
  Single<Boolean> execute();
  default String name(){
    return getClass().getName();
  }
}

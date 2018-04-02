package com.github.yassine.soxychains.core;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RunOn {
  Phase value();
}

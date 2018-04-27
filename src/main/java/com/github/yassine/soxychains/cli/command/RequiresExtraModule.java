package com.github.yassine.soxychains.cli.command;

import com.google.inject.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresExtraModule {
  Class<? extends Module>[] value() default {};
}

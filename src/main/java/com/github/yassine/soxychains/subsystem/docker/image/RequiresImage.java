package com.github.yassine.soxychains.subsystem.docker.image;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresImage {
  String name();
  String resourceRoot();
}

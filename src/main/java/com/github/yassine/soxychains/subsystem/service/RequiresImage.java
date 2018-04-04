package com.github.yassine.soxychains.subsystem.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresImage {
  String name();
  String resourceRoot();
  boolean injectAsTemplateParameter() default true;
  String injectionParamKey() default "config";
}

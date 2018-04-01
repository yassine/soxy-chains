package com.github.yassine.soxychains.cli;

import io.airlift.airline.Help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface CommandGroup {
  String name();
  String description() default "";
  Class  defaultCommand() default Help.class;
}

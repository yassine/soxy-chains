package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) @BindingAnnotation
public @interface ClassPath {
}

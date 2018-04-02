package com.github.yassine.soxychains.subsystem.docker.image.resolver;


import java.io.InputStream;
import java.util.Map;
import java.util.function.Predicate;

public interface DockerImageResourceResolver {

  InputStream resolve(String path, Map<String, ?> context);
  InputStream resolve(String path, Map<String, ?> context, Predicate<String> includePredicate);

  Predicate<String> DEFAULT_INCLUDE_PREDICATE  = (path) -> !path.endsWith(".class");
  Predicate<String> DEFAULT_TEMPLATE_PREDICATE = (path) -> path.endsWith(".template") || path.endsWith(".tpl");
}

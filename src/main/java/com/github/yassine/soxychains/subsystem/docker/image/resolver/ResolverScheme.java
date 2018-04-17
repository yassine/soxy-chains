package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.github.yassine.soxychains.core.SoxyChainsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;

@RequiredArgsConstructor @Getter @Accessors(fluent = true)
public enum ResolverScheme {
  CLASSPATH("classpath"),
  FILESYSTEM("file");
  private final String scheme;
  static ResolverScheme schemeValueOf(String scheme){
    return Arrays.stream(ResolverScheme.values())
      .filter(resolverScheme -> resolverScheme.scheme().equals(scheme))
      .findAny()
      .orElseThrow(() -> new SoxyChainsException(String.format("Invalid scheme '%s'", scheme)));
  }
}

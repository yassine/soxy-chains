package com.github.yassine.soxychains.web;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import io.undertow.Undertow;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class WebAPI {

  private final WebAPIConfiguration configuration;

  private final LoadingCache<String, Undertow> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Undertow>() {
    @Override
    public Undertow load(String s) throws Exception {
      return Undertow.builder()
              .addHttpListener(configuration.getPort(), configuration.getBindAddress())
              .build();
    }
  });

  @SneakyThrows
  public void startup(){
    cache.get("").start();
  }

  @SneakyThrows
  public void stop(){
    cache.get("").stop();
  }

}

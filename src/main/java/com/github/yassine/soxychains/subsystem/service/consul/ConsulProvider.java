package com.github.yassine.soxychains.subsystem.service.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Singleton @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ConsulProvider {

  private final ServicesConfiguration servicesConfiguration;

  private LoadingCache<DockerHostConfiguration, ConsulClient> cache = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, ConsulClient>() {
    @Override
    public ConsulClient load(DockerHostConfiguration dockerHostConfiguration) {
      ConsulConfiguration consulConfiguration = (ConsulConfiguration) servicesConfiguration.get(ConsulService.class);
      return new ConsulClient(String.format("%s:%s", dockerHostConfiguration.getHostname(), consulConfiguration.getServicePort().toString()));
    }
  });

  @SneakyThrows
  public ConsulClient get(DockerHostConfiguration dockerHostConfiguration){
    return cache.get(dockerHostConfiguration);
  }

}

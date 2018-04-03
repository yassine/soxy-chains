package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

import static net.jodah.typetools.TypeResolver.resolveRawArgument;

public class ServicesModule extends PrivateModule{
  @Override @SuppressWarnings("unchecked")
  protected void configure() {
    GuiceUtils.loadSPIClasses(ServicesPlugin.class)
      .stream()
      .map(clazz -> (Class) resolveRawArgument(ServicesPlugin.class, clazz))
      .forEach(configClass -> {
        PluginConfigProvider provider = new PluginConfigProvider(configClass);
        requestInjection(provider);
        bind(configClass).toProvider(provider);
        expose(configClass);
      });
  }

  @RequiredArgsConstructor
  public static class PluginConfigProvider<CONFIG > implements Provider<CONFIG> {
    private final Class<? extends CONFIG> clazz;
    @Inject
    private Provider<ServicesConfiguration> servicesConfiguration;
    @Override @SuppressWarnings("unchecked")
    public CONFIG get() {
      return (CONFIG) servicesConfiguration.get().configurations().stream()
        .filter(config -> clazz.equals(config.getClass()))
        .findFirst()
        .orElse(null);
    }
  }

  @Provides
  ServicesConfiguration servicesConfiguration(SoxyChainsConfiguration soxyChainsConfiguration, Injector injector){
    return Optional.ofNullable(soxyChainsConfiguration.getServices())
            .orElse(defaultServicesConfiguration(injector));
  }

  @SuppressWarnings("unchecked")
  static ServicesConfiguration defaultServicesConfiguration(Injector injector){
    ImmutableMap.Builder<String, ServicesPluginConfiguration> builder = ImmutableMap.builder();
    GuiceUtils.loadSPIClasses(ServicesPlugin.class).stream()
      .map(injector::getInstance)
      .map(plugin -> Pair.of(plugin.configKey(), plugin.defaultConfiguration()))
      .forEach(p -> builder.put(p.getKey(), p.getValue()));
    return new ServicesConfiguration(builder.build());
  }
}

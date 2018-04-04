package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.plugin.PluginUtils;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;

import static net.jodah.typetools.TypeResolver.resolveRawArgument;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

@Slf4j
public class ServicesModule extends AbstractModule{
  @Override @SuppressWarnings("unchecked")
  protected void configure() {
    Set<Class<? extends ServicesPlugin>> pluginClasses = GuiceUtils.loadSPIClasses(ServicesPlugin.class);
    pluginClasses.stream()
      .filter(clazz -> resolveRawArguments(ServicesPlugin.class, clazz).length == 0)
      .forEach(clazz ->
        log.warn("Plugin '{}' ignored,  couldn't detect/resolve its config class." +
                 "Plugin class has most likely been declared as type-erased.", clazz.getName()));
    Multibinder<ServicesPlugin> pluginMultibinder = Multibinder.newSetBinder(binder(), ServicesPlugin.class);
    pluginClasses.stream()
      .filter(pluginClass -> resolveRawArguments(ServicesPlugin.class, pluginClass).length == 0)
      .forEach(pluginClass -> pluginMultibinder.addBinding().to(pluginClass));
    pluginClasses.stream()
      .filter(pluginClass -> resolveRawArguments(ServicesPlugin.class, pluginClass).length == 1)
      .map(configClass -> (Class) resolveRawArgument(ServicesPlugin.class, configClass))
      .forEach(configClass -> {
        PluginConfigProvider provider = new PluginConfigProvider(configClass);
        requestInjection(provider);
        bind(configClass).toProvider(provider);
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

  @Provides @Singleton
  ServicesConfiguration servicesConfiguration(SoxyChainsConfiguration soxyChainsConfiguration, Injector injector){
    return Optional.ofNullable(soxyChainsConfiguration.getServices())
            .orElse(defaultServicesConfiguration());
  }

  @SuppressWarnings("unchecked")
  static ServicesConfiguration defaultServicesConfiguration(){
    ImmutableMap.Builder<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration> builder = ImmutableMap.builder();
    GuiceUtils.loadSPIClasses(ServicesPlugin.class).stream()
      .forEach(pluginClass -> builder.put((Class<? extends Plugin<ServicesPluginConfiguration>>) pluginClass, (ServicesPluginConfiguration) PluginUtils.defaultConfig(pluginClass)));
    return new ServicesConfiguration(builder.build());
  }
}

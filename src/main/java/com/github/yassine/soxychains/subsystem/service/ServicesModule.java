package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.plugin.PluginConfiguration;
import com.github.yassine.soxychains.plugin.PluginUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;
import java.util.Set;

import static net.jodah.typetools.TypeResolver.resolveRawArgument;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

@Slf4j
public class ServicesModule extends AbstractModule{
  @Override @SuppressWarnings("unchecked")
  protected void configure() {
    Multibinder<ServicesPlugin> pluginMultibinder = Multibinder.newSetBinder(binder(), ServicesPlugin.class);
    Set<Class<? extends ServicesPlugin>> pluginClasses = GuiceUtils.loadSPIClasses(ServicesPlugin.class);
    pluginClasses.stream()
      .filter(clazz -> resolveRawArguments(ServicesPlugin.class, clazz).length == 0)
      .forEach(clazz ->
        log.warn("Plugin '{}' ignored,  couldn't detect/resolve its config class." +
                 "Plugin class has most likely been declared without type parameters.", clazz.getName()));
    pluginClasses.stream()
      .filter(pluginClass -> resolveRawArguments(ServicesPlugin.class, pluginClass).length == 1)
      .forEach(pluginClass -> {
        bind((Class) pluginClass).annotatedWith(Internal.class).to(pluginClass);
        pluginMultibinder.addBinding().toProvider(new PluginProvider(pluginClass)).in(Singleton.class);
        PluginConfigProvider provider = new PluginConfigProvider(pluginClass);
        requestInjection(provider);
        bind((Class) resolveRawArgument(ServicesPlugin.class, pluginClass)).toProvider(provider);
      });
  }

  @RequiredArgsConstructor
  static class PluginProvider implements Provider<ServicesPlugin> {
    private final Class<? extends ServicesPlugin> pluginClass;
    @Inject
    private Injector injector;
    private LoadingCache<String, ServicesPlugin> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, ServicesPlugin>() {
      @Override
      public ServicesPlugin load(String s) throws Exception {
        PluginConfiguration configuration = (PluginConfiguration) injector.getInstance((Class) resolveRawArgument(ServicesPlugin.class, pluginClass));
        ServicesPlugin plugin = injector.getInstance(Key.get(TypeLiteral.get(pluginClass), Internal.class));
        plugin.configure(configuration);
        return plugin;
      }
    });
    @Override @SneakyThrows
    public ServicesPlugin get() {
      return cache.get("");
    }
  }

  @BindingAnnotation @Retention(RetentionPolicy.RUNTIME)
  @interface Internal {

  }

  @RequiredArgsConstructor
  public static class PluginConfigProvider<C extends ServicesPluginConfiguration> implements Provider<C> {
    private final Class<? extends Plugin<? extends C>> clazz;
    @Inject
    private Provider<ServicesConfiguration> servicesConfiguration;
    @Override @SuppressWarnings("unchecked")
    public C get() {
      return (C) servicesConfiguration.get().get(clazz);
    }
  }

  @Provides @Singleton
  ServicesConfiguration servicesConfiguration(SoxyChainsContext soxyChainsContext){
    return Optional.ofNullable(soxyChainsContext.getDocker().getServices())
            .orElse(defaultServicesConfiguration());
  }

  @SuppressWarnings("unchecked")
  static ServicesConfiguration defaultServicesConfiguration(){
    ImmutableMap.Builder<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration> builder = ImmutableMap.builder();
    GuiceUtils.loadSPIClasses(ServicesPlugin.class).stream()
      .map(clazz -> (Class<? extends Plugin<ServicesPluginConfiguration>>) clazz)
      .forEach(pluginClass -> builder.put(pluginClass, PluginUtils.defaultConfig(pluginClass)));
    return new ServicesConfiguration(builder.build());
  }
}

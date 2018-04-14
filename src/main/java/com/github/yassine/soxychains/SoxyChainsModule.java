package com.github.yassine.soxychains;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.yassine.artifacts.guice.scheduling.TaskSchedulerModule;
import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.github.yassine.soxychains.core.CoreModule;
import com.github.yassine.soxychains.core.PluginsConfigDeserializer;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.DockerModule;
import com.github.yassine.soxychains.subsystem.layer.LayerModule;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesModule;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.jodah.typetools.TypeResolver;

import javax.validation.*;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class SoxyChainsModule extends AbstractModule {

  private final InputStream configStream;

  @Override
  protected void configure() {
    install(new ConfigurationModule(configStream));
    install(new DockerModule());
    install(new ServicesModule());
    install(new CoreModule());
    install(new TaskSchedulerModule());
    install(new LayerModule());
  }

  @RequiredArgsConstructor @Slf4j
  static class ConfigurationModule extends AbstractModule{

    private final InputStream configStream;

    @SuppressWarnings("unchecked")
    @Provides @Singleton @Configuration
    ObjectMapper getConfigurationMapper(){
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
      mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      registerLayerSubtypes(mapper);
      SimpleModule simpleModule = new SimpleModule();
      simpleModule.addDeserializer(ServicesConfiguration.class,
        new PluginsConfigDeserializer( ServicesPlugin.class,
          (map) -> new ServicesConfiguration((Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration>) map)));
      mapper.registerModule(simpleModule);

      return mapper;
    }

    private void registerLayerSubtypes(ObjectMapper mapper){
      Set<Class<? extends LayerProvider>> implementations = GuiceUtils.loadSPIClasses(LayerProvider.class);
      implementations.stream()
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .filter(clazz -> TypeResolver.resolveRawArguments(LayerProvider.class, clazz).length == 0)
        .forEach(clazz -> log.warn("Unable to register Class '{}'. Declarative type-erasure ?", clazz));
      implementations.stream()
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .filter(clazz -> TypeResolver.resolveRawArguments(LayerProvider.class, clazz).length >= 1)
        .forEach(clazz -> mapper.registerSubtypes(TypeResolver.resolveRawArguments(LayerProvider.class, clazz)[0]));
    }

    @SuppressWarnings("unchecked")
    @Provides @Singleton
    ObjectMapper getDefaultConfigurationMapper(){
      ObjectMapper mapper = new ObjectMapper();
      mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
      return mapper;
    }

    @Provides @Singleton @SneakyThrows
    SoxyChainsConfiguration getConfiguration(@Configuration ObjectMapper mapper, @Configuration Validator validator){
      SoxyChainsConfiguration configuration = mapper.readValue(configStream, SoxyChainsConfiguration.class);
      Set<ConstraintViolation<SoxyChainsConfiguration>> constraintViolations = validator.validate(configuration);
      if(constraintViolations.size() > 0){
        throw new ConstraintViolationException(constraintViolations);
      }
      return configuration;
    }

    @Provides @Singleton @Configuration
    Validator getValidator(){
      ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      return factory.getValidator();
    }

    @Override
    protected void configure() {

    }
  }

  @Retention(RetentionPolicy.RUNTIME) @BindingAnnotation
  public @interface Configuration{}

}

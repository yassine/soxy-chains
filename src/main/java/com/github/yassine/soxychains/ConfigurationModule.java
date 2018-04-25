package com.github.yassine.soxychains;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.yassine.artifacts.guice.utils.GuiceUtils;
import com.github.yassine.soxychains.core.PluginsConfigDeserializer;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.*;
import lombok.AccessLevel;
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

@Slf4j
public class ConfigurationModule extends AbstractModule {

  private final ConfigurationProvider configurationProvider;

  public ConfigurationModule(InputStream configStream) {
    this.configurationProvider = new ConfigurationProvider(configStream);
  }

  @Override
  protected void configure() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    factory.close();
    bind(Validator.class).annotatedWith(Configuration.class).toInstance(validator);
    bind(SoxyChainsContext.class).toProvider(configurationProvider).in(Singleton.class);
  }


  @SuppressWarnings("unchecked")
  @Provides
  @Singleton
  @Configuration
  ObjectMapper getConfigurationMapper(){
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    registerLayerSubtypes(mapper);
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(ServicesConfiguration.class,
      new PluginsConfigDeserializer( ServicesPlugin.class,
        map -> new ServicesConfiguration((Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration>) map)));
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




  @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
  public static class ConfigurationProvider implements Provider<SoxyChainsContext> {

    private final InputStream configStream;

    @Configuration @Inject
    private ObjectMapper mapper;
    @Configuration @Inject
    private Validator validator;

    private LoadingCache<String, SoxyChainsContext> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, SoxyChainsContext>() {
      @Override
      public SoxyChainsContext load(String s) throws Exception {
        SoxyChainsContext configuration = mapper.readValue(configStream, SoxyChainsContext.class);
        Set<ConstraintViolation<SoxyChainsContext>> constraintViolations = validator.validate(configuration);
        if(!constraintViolations.isEmpty()){
          throw new ConstraintViolationException(constraintViolations);
        }
        return configuration;
      }
    });

    @Override @SneakyThrows
    public SoxyChainsContext get() {
      return cache.get("");
    }
  }

  @Retention(RetentionPolicy.RUNTIME) @BindingAnnotation
  @interface Configuration{}
}

package com.github.yassine.soxychains;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

@RequiredArgsConstructor
class SoxyChainsModule extends AbstractModule {

  private final InputStream configStream;

  @Override
  protected void configure() {
    install(new ConfigurationModule(configStream));
  }

  @RequiredArgsConstructor
  static class ConfigurationModule extends AbstractModule{

    private final InputStream configStream;

    @Provides
    @Singleton
    @Configuration
    ObjectMapper getConfigurationMapper(){
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
      return mapper;
    }

    @Provides @Singleton @SneakyThrows
    SoxyChainsConfiguration getConfiguration(@Configuration ObjectMapper mapper, @Configuration Validator validator){
      SoxyChainsConfiguration configuration = mapper.readValue(configStream, SoxyChainsConfiguration.class);
      Set<ConstraintViolation<SoxyChainsConfiguration>> constraintViolations = validator.validate(configuration);
      if(constraintViolations.size() > 0){
        StringBuilder sb = new StringBuilder();
        constraintViolations.stream().forEach(s-> {
          sb.append("\'");
          sb.append(s.getPropertyPath());
          sb.append("\' field @ \'");
          sb.append(s.getLeafBean().getClass().getName());
          sb.append("\' ");
          sb.append(s.getMessage());
          sb.append('\n');
        });
        throw new RuntimeException(sb.toString());
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
  @interface Configuration{}

}

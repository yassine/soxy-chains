package com.github.yassine.soxychains;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.yassine.soxychains.core.PluginsConfigDeserializer;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;

import java.util.Map;

public class TestUtils {

  @SuppressWarnings("unchecked")
  public static ObjectMapper mapper() {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    om.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(ServicesConfiguration.class,
      new PluginsConfigDeserializer( ServicesPlugin.class,
        (map) -> new ServicesConfiguration((Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration>) map)));
    om.registerModule(simpleModule);
    return om;
  }

  public static  DockerClient dockerClient(DockerHostConfiguration hostConfiguration) {
    return DockerClientBuilder.getInstance(hostConfiguration.getUri().toString()).build();
  }

}

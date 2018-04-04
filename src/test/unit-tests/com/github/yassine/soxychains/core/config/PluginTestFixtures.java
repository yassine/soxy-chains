package com.github.yassine.soxychains.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.yassine.soxychains.core.PluginsConfigDeserializer;
import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.plugin.PluginConfiguration;
import com.github.yassine.soxychains.plugin.PluginSetConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import lombok.Getter;

public class PluginTestFixtures {

  public interface TestPlugin<CONFIG extends PluginConfiguration> extends Plugin<CONFIG> {}

  @AutoService(TestPlugin.class)
  public static class PluginA implements TestPlugin<PluginAConfiguration>, Plugin<PluginAConfiguration> {
  }
  @Getter  @ConfigKey("pluginA")
  public static class PluginAConfiguration implements PluginConfiguration{
    private String paramA;
  }
  @AutoService(TestPlugin.class)
  public static class PluginB implements TestPlugin<PluginBConfiguration>, Plugin<PluginBConfiguration> {
  }
  @Getter @ConfigKey("pluginB")
  public static class PluginBConfiguration implements PluginConfiguration{
    private String paramB;
  }
  @Getter
  public static class RootConfiguration {
    PluginSetConfiguration plugins;
  }
  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
      mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      SimpleModule module   = new SimpleModule();
      module.addDeserializer(PluginSetConfiguration.class, new PluginsConfigDeserializer(TestPlugin.class));
      mapper.registerModule(module);
      bind(ObjectMapper.class).toInstance(mapper);
    }
  }
}

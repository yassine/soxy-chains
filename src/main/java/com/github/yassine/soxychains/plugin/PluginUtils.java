package com.github.yassine.soxychains.plugin;

import lombok.SneakyThrows;

import static net.jodah.typetools.TypeResolver.resolveRawArgument;

public class PluginUtils {

  private PluginUtils() {}

  @SneakyThrows
  public static <CONFIG extends PluginConfiguration> CONFIG defaultConfig(Class <? extends Plugin<CONFIG>> pluginContract){
    return configClassOf(pluginContract).newInstance();
  }

  public static <CONFIG extends PluginConfiguration> Class<CONFIG> configClassOf(Class <? extends Plugin<CONFIG>> pluginContract){
    return (Class<CONFIG>) resolveRawArgument(Plugin.class, pluginContract);
  }
  public static String configKey(Class<? extends Plugin> pluginContract){
    Class<? extends PluginConfiguration> configClass = ((Class<? extends PluginConfiguration>) resolveRawArgument(Plugin.class, pluginContract));
    if(configClass.isAnnotationPresent(ConfigKey.class)){
      return configClass.getAnnotation(ConfigKey.class).value();
    }else{
      return configClass.getSimpleName();
    }
  }
}

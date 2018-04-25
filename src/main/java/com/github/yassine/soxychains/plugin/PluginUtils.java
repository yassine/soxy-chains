package com.github.yassine.soxychains.plugin;

import lombok.SneakyThrows;

import static net.jodah.typetools.TypeResolver.resolveRawArgument;

public class PluginUtils {

  private PluginUtils() {}

  @SneakyThrows
  public static <C extends PluginConfiguration> C defaultConfig(Class <? extends Plugin<C>> pluginContract){
    return configClassOf(pluginContract).newInstance();
  }

  public static <C extends PluginConfiguration> Class<C> configClassOf(Class <? extends Plugin<C>> pluginContract){
    return (Class<C>) resolveRawArgument(Plugin.class, pluginContract);
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

package com.github.yassine.soxychains.plugin;

public interface PluginSetConfiguration<C extends PluginConfiguration> {
  C get(Class<? extends Plugin<? extends C>> pluginClass);
}

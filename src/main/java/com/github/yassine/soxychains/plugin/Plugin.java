package com.github.yassine.soxychains.plugin;

@SuppressWarnings("unused")
public interface Plugin<C extends PluginConfiguration> {
  default void configure(C configuration){}
}

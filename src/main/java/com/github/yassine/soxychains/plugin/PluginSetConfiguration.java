package com.github.yassine.soxychains.plugin;

public interface PluginSetConfiguration<CONFIG extends PluginConfiguration> {
  CONFIG get(String pluginConfigKey);
}
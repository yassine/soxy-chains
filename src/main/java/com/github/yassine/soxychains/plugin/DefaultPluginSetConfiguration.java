package com.github.yassine.soxychains.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultPluginSetConfiguration<PLUGIN_CONFIG extends PluginConfiguration> implements PluginSetConfiguration<PLUGIN_CONFIG>{

  @Getter(AccessLevel.PROTECTED)
  private final Map<String, PLUGIN_CONFIG> delegate;

  @Override
  public PLUGIN_CONFIG get(String pluginConfigKey) {
    return delegate.get(pluginConfigKey);
  }

}

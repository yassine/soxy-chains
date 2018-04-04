package com.github.yassine.soxychains.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultPluginSetConfiguration<PLUGIN_CONFIG extends PluginConfiguration> implements PluginSetConfiguration<PLUGIN_CONFIG>{

  @Getter(AccessLevel.PROTECTED)
  private final Map<Class<? extends Plugin<PLUGIN_CONFIG>>, PLUGIN_CONFIG> delegate;

  @Override
  public PLUGIN_CONFIG get(Class<? extends Plugin<? extends PLUGIN_CONFIG>> pluginClass) {
    return delegate.get(pluginClass);
  }

}

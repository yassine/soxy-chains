package com.github.yassine.soxychains.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultPluginSetConfiguration<C extends PluginConfiguration> implements PluginSetConfiguration<C>{

  @Getter(AccessLevel.PROTECTED)
  private final Map<Class<? extends Plugin<C>>, C> delegate;

  @Override
  public C get(Class<? extends Plugin<? extends C>> pluginClass) {
    return delegate.get(pluginClass);
  }

}

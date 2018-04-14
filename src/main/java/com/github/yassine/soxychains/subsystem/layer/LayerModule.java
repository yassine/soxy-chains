package com.github.yassine.soxychains.subsystem.layer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.jodah.typetools.TypeResolver;
import org.jvnet.tiger_types.Types;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;

public class LayerModule extends AbstractModule{
  @Override
  protected void configure() {
    MapBinder<Class<? extends AbstractLayerConfiguration>, LayerProvider> mapBinder =
      (MapBinder<Class<? extends AbstractLayerConfiguration>, LayerProvider>) MapBinder.newMapBinder(binder(), TypeLiteral.get(Types.createParameterizedType(Class.class, com.google.inject.util.Types.subtypeOf(AbstractLayerConfiguration.class))), TypeLiteral.get(LayerProvider.class));
    loadSPIClasses(LayerProvider.class).stream()
      .filter(clazz -> TypeResolver.resolveRawArguments(LayerProvider.class, clazz).length > 0)
      .forEach(clazz -> {
        Class<? extends AbstractLayerConfiguration> configClass = (Class<? extends AbstractLayerConfiguration>) TypeResolver.resolveRawArguments(LayerProvider.class, clazz)[0];
        mapBinder.addBinding(configClass).to(clazz);
      });
    bind(LayerService.class).to(LayerServiceSupport.class);
    bind(LayerManager.class).in(Singleton.class);
  }
}

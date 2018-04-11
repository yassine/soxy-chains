package com.github.yassine.soxychains.subsystem.layer;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.jodah.typetools.TypeResolver;
import org.jvnet.tiger_types.Types;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;

public class LayerModule extends AbstractModule{
  @Override
  protected void configure() {
    MapBinder<Class<? extends AbstractLayerConfiguration>, LayerService> mapBinder =
      (MapBinder<Class<? extends AbstractLayerConfiguration>, LayerService>) MapBinder.newMapBinder(binder(), TypeLiteral.get(Types.createParameterizedType(Class.class, com.google.inject.util.Types.subtypeOf(AbstractLayerConfiguration.class))), TypeLiteral.get(Types.createParameterizedType(LayerService.class)));
    loadSPIClasses(LayerService.class).stream()
      .filter(clazz -> TypeResolver.resolveRawArguments(LayerService.class, clazz).length > 0)
      .forEach(clazz -> {
        Class<? extends AbstractLayerConfiguration> configClass = (Class<? extends AbstractLayerConfiguration>) TypeResolver.resolveRawArguments(LayerService.class, clazz)[0];
        mapBinder.addBinding(configClass).to(clazz);
      });
  }
}

package com.github.yassine.soxychains.subsystem.layer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.jvnet.tiger_types.Types;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

public class LayerModule extends AbstractModule{
  @Override
  protected void configure() {
    MapBinder<Class<? extends AbstractLayerConfiguration>, LayerProvider> mapBinder =
      (MapBinder<Class<? extends AbstractLayerConfiguration>, LayerProvider>) MapBinder.newMapBinder(binder(), TypeLiteral.get(Types.createParameterizedType(Class.class, com.google.inject.util.Types.subtypeOf(AbstractLayerConfiguration.class))), TypeLiteral.get(LayerProvider.class));
    Multibinder<LayerProvider> layerProviders = Multibinder.newSetBinder(binder(), LayerProvider.class);
    loadSPIClasses(LayerProvider.class).stream()
      .filter( clazz -> resolveRawArguments(LayerProvider.class, clazz).length > 0 )
      .forEach( clazz -> {
        Class<? extends AbstractLayerConfiguration> configClass = (Class<? extends AbstractLayerConfiguration>) resolveRawArguments(LayerProvider.class, clazz)[0];
        mapBinder.addBinding(configClass).to(clazz);
      });
    loadSPIClasses(LayerProvider.class).forEach(clazz -> layerProviders.addBinding().to(clazz));
    Multibinder<LayerObserver> layerObservers = Multibinder.newSetBinder(binder(), LayerObserver.class);
    loadSPIClasses(LayerObserver.class).forEach(clazz -> layerObservers.addBinding().to(clazz));
    bind(LayerService.class).to(LayerServiceSupport.class);
    bind(LayerManager.class).in(Singleton.class);
  }
}

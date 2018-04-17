package com.github.yassine.soxychains.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.yassine.soxychains.plugin.DefaultPluginSetConfiguration;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.plugin.PluginConfiguration;
import com.github.yassine.soxychains.plugin.PluginSetConfiguration;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;
import static com.github.yassine.soxychains.plugin.PluginUtils.configKey;
import static com.github.yassine.soxychains.plugin.PluginUtils.defaultConfig;
import static com.machinezoo.noexception.Exceptions.sneak;
import static java.lang.String.format;
import static net.jodah.typetools.TypeResolver.resolveRawArgument;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

@Slf4j
public class PluginsConfigDeserializer<CONFIG extends PluginSetConfiguration<?>> extends StdDeserializer<CONFIG>{

  private final Class<? extends Plugin> pluginContract;
  private transient Function<Map<Class<? extends Plugin>, PluginConfiguration>, CONFIG> transform = map -> (CONFIG) new DefaultPluginSetConfiguration(map);

  public PluginsConfigDeserializer(Class<? extends Plugin> pluginContract, Function<Map<Class<? extends Plugin>, PluginConfiguration>, CONFIG> transform) {
    super(PluginSetConfiguration.class);
    this.pluginContract = pluginContract;
    this.transform = transform;
  }

  public PluginsConfigDeserializer(Class<? extends Plugin> pluginContract) {
    super(PluginSetConfiguration.class);
    this.pluginContract = pluginContract;
  }

  @Override
  public CONFIG deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    ObjectNode root     = mapper.readTree(jp);
    PropertyNamingStrategy propertyNamingStrategy = mapper.getPropertyNamingStrategy();
    List<Class<? extends Plugin>> pluginImplementations = loadSPIClasses(pluginContract).stream()
                            .filter(clazz -> resolveRawArguments(Plugin.class, clazz).length > 0)
                            .collect(Collectors.toList());
    ImmutableMap.Builder<Class<? extends Plugin>, PluginConfiguration> builder = ImmutableMap.builder();
    stream(root.fields())
      .filter(field -> !StringUtils.isEmpty(field.getKey()))
      .flatMap( field -> pluginImplementations.stream()
        .filter(pluginClass -> equalKeys(propertyNamingStrategy, field.getKey(), pluginClass))
        .map(pluginClass -> Pair.of(pluginClass, (PluginConfiguration) safeRead(mapper, pluginClass, field.getValue().toString())))
      ).forEach(p -> builder.put(p.getKey(), p.getValue()));
    pluginImplementations.stream()
      .filter(pluginClass -> stream(root.fields()).map(Map.Entry::getKey).noneMatch(key -> equalKeys(propertyNamingStrategy, key, pluginClass)) )
      .forEach(pluginClass -> {
        Class<? extends PluginConfiguration> pluginConfigClass = (Class<? extends PluginConfiguration>) resolveRawArgument(Plugin.class, pluginClass);
        Arrays.stream(pluginConfigClass.getConstructors())
          .filter(constructor -> constructor.getParameterCount() == 0)
          .findAny()
          .orElseThrow( () -> new SoxyChainsException(format("'%s' as implementation of '%s' contract, must have a no-arg constructor.", pluginClass.getName(), PluginConfiguration.class)) );
        PluginConfiguration config = sneak().get(pluginConfigClass::newInstance);
        builder.put(pluginClass, config);
      });
    return transform.apply(builder.build());
  }

  @SneakyThrows
  private <CONFIG extends PluginConfiguration> CONFIG safeRead(ObjectMapper mapper, Class<? extends Plugin> pluginClass, String value) {
    CONFIG config = (CONFIG) mapper.readValue(value, resolveRawArgument(Plugin.class, pluginClass));
    return config != null ? config : defaultConfig(pluginClass);
  }

  private boolean equalKeys(PropertyNamingStrategy propertyNamingStrategy, String foundKey, Class<? extends Plugin> pluginClass){
    return foundKey.equals(configKey(pluginClass)) || foundKey.equals(translate(configKey(pluginClass), propertyNamingStrategy));
  }

  private String translate(String name, PropertyNamingStrategy propertyNamingStrategy){
    if(propertyNamingStrategy instanceof PropertyNamingStrategy.PropertyNamingStrategyBase){
      PropertyNamingStrategy.PropertyNamingStrategyBase namingStrategy = (PropertyNamingStrategy.PropertyNamingStrategyBase) propertyNamingStrategy;
      return namingStrategy.translate(name);
    }
    return name;
  }

  private <T> Stream<T> stream(Iterator<T> iterator){
    Iterable<T> iterable = () -> iterator;
    return StreamSupport.stream(iterable.spliterator(), false);
  }


}

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
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;
import static com.google.common.collect.Streams.stream;
import static java.util.Optional.ofNullable;

public class PluginsConfigDeserializer<CONFIG extends PluginSetConfiguration<?>> extends StdDeserializer<CONFIG>{

  private final Class<? extends Plugin> pluginContract;
  private Function<Map<String, PluginConfiguration>, CONFIG> transform = (map) -> (CONFIG) new DefaultPluginSetConfiguration(map);
  @Inject
  private static Injector injector;

  public PluginsConfigDeserializer(Class<? extends Plugin> pluginContract, Function<Map<String, PluginConfiguration>, CONFIG> transform) {
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
    List<Plugin> plugins = loadSPIClasses(pluginContract).stream()
                            .map(injector::getInstance)
                            .collect(Collectors.toList());
    ImmutableMap.Builder<String, PluginConfiguration> builder = ImmutableMap.builder();
    stream(root.fields())
      .filter(field -> !StringUtils.isEmpty(field.getKey()))
      .flatMap( field -> plugins.stream()
        .filter(plugin -> plugin.configKey().equals(field.getKey()) || translate(plugin.configKey(), propertyNamingStrategy).equals(field.getKey()))
        .map(plugin -> ofNullable(safeRead(mapper, plugin.defaultConfiguration().getClass(), field.getValue().toString()))
                          .map(config -> Pair.of(plugin.configKey(), config))
                          .orElse(Pair.of(plugin.configKey(), plugin.defaultConfiguration())))
      ).forEach(p -> {
        builder.put(p.getKey(), p.getValue());
        if(!p.getKey().equals(translate(p.getKey(), propertyNamingStrategy))){
          builder.put(translate(p.getKey(), propertyNamingStrategy), p.getValue());
        }
      });
    //noinspection unchecked
    return transform.apply(builder.build());
  }

  @SneakyThrows
  private PluginConfiguration safeRead(ObjectMapper mapper, Class<? extends PluginConfiguration> configurationClass, String value) {
    return mapper.readValue(value, configurationClass);
  }

  private String translate(String name, PropertyNamingStrategy propertyNamingStrategy){
    if(propertyNamingStrategy instanceof PropertyNamingStrategy.PropertyNamingStrategyBase){
      PropertyNamingStrategy.PropertyNamingStrategyBase namingStrategy = (PropertyNamingStrategy.PropertyNamingStrategyBase) propertyNamingStrategy;
      return namingStrategy.translate(name);
    }
    return name;
  }

}

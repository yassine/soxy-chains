package com.github.yassine.soxychains.plugin;

import lombok.SneakyThrows;
import net.jodah.typetools.TypeResolver;

import static com.google.common.collect.Lists.reverse;
import static java.util.Arrays.asList;

public interface Plugin<CONFIG extends PluginConfiguration> {

  default String configKey(){
    return reverse(asList(getClass().getPackage().getName().split("\\."))).get(0);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  default CONFIG defaultConfiguration(){
    Class type = TypeResolver.resolveRawArgument(Plugin.class, getClass());
    return (CONFIG) type.newInstance();
  }

}

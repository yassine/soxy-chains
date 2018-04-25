package com.github.yassine.soxychains.subsystem.layer.spi.tor;


import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;

@AutoService(LayerProvider.class)
public class TorLayerProvider implements LayerProvider<TorLayerContext, TorNodeConfiguration> {

  private static final DockerImage IMAGE = new DockerImage("tor-node", URI.create("classpath://com/github/yassine/soxychains/subsystem/layer/spi/tor"), ImmutableMap.of());
  private static final String CONFIG_SEPARATOR = ",";

  @Override
  public void configureNode(CreateContainerCmd containerCmd, TorNodeConfiguration nodeConfiguration, TorLayerContext layerConfiguration) {
    // node configuration takes precedence over the layer configuration
    containerCmd.withEnv(
      Stream.of(
        getConfig(nodeConfiguration.getEntryNodes(),layerConfiguration.getEntryNodes())
          .map(entryNodes -> format("TOR_ENTRY_NODES=%s", entryNodes)),
        getConfig(nodeConfiguration.getExitNodes(),layerConfiguration.getExitNodes())
          .map(exitNodes -> format("TOR_EXIT_NODES=%s", exitNodes)),
        getConfig(nodeConfiguration.getExcludeExitNodes(),layerConfiguration.getExcludeExitNodes())
          .map(excludeExitNodes -> format("TOR_EXCLUDE_EXIT_NODES=%s", excludeExitNodes)),
        getConfig(nodeConfiguration.getExcludeNodes(),layerConfiguration.getExcludeNodes())
          .map(excludeExitNodes -> format("TOR_EXCLUDE_NODES=%s", excludeExitNodes))
      )
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList())
    );
  }

  private Optional<String> getConfig(Set<String> principal, Set<String> fallback){
    return Optional.ofNullable(Optional.ofNullable(principal).orElse(fallback)).map(this::caonicalize);
  }
  private String caonicalize(Set<String> values){
    return Joiner.on(CONFIG_SEPARATOR).join(values.stream().flatMap(entryNode -> stream(entryNode.split(CONFIG_SEPARATOR))).collect(Collectors.toSet()));
  }

  @Override
  public DockerImage image(TorLayerContext layerConfiguration) {
    return IMAGE;
  }

}

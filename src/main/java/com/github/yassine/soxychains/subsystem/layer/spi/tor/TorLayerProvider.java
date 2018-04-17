package com.github.yassine.soxychains.subsystem.layer.spi.tor;


import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@AutoService(LayerProvider.class)
public class TorLayerProvider implements LayerProvider<TorLayerConfiguration, TorNodeConfiguration> {

  private static final DockerImage IMAGE = new DockerImage("tor-node", URI.create("classpath://com/github/yassine/soxychains/subsystem/layer/spi/tor"), ImmutableMap.of());

  @Override
  public void configureNode(CreateContainerCmd containerCmd, TorNodeConfiguration nodeConfiguration, TorLayerConfiguration layerConfiguration) {
    // node configuration takes precedence over the layer configuration
    containerCmd.withEnv(
      Stream.of(
        ofNullable(nodeConfiguration.getEntryNodes())
          .map(entryNodes -> String.format("TOR_ENTRY_NODES=%s", entryNodes))
          .orElse(ofNullable(layerConfiguration.getEntryNodes())
            .map(entryNodes -> String.format("TOR_ENTRY_NODES=%s", entryNodes))
            .orElse("")),
        ofNullable(nodeConfiguration.getExitNodes())
          .map(exitNodes -> String.format("TOR_EXIT_NODES=%s", exitNodes))
          .orElse(ofNullable(layerConfiguration.getExitNodes())
            .map(exitNodes -> String.format("TOR_EXIT_NODES=%s", exitNodes))
            .orElse("")),
        ofNullable(nodeConfiguration.getExcludeExitNodes())
          .map(excludeExitNodes -> String.format("TOR_EXCLUDE_EXIT_NODES=%s", excludeExitNodes))
          .orElse(ofNullable(layerConfiguration.getExcludeExitNodes())
            .map(excludeExitNodes -> String.format("TOR_EXCLUDE_EXIT_NODES=%s", excludeExitNodes))
            .orElse("")),
        ofNullable(nodeConfiguration.getExcludeNodes())
          .map(excludeNodes -> String.format("TOR_EXCLUDE_NODES=%s", excludeNodes))
          .orElse(ofNullable(layerConfiguration.getExcludeNodes())
            .map(excludeNodes -> String.format("TOR_EXCLUDE_NODES=%s", excludeNodes))
            .orElse(""))
      )
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.toList())
    );
  }

  @Override
  public boolean matches(Container container, TorNodeConfiguration nodeConfiguration, TorLayerConfiguration layerConfiguration) {
    return true;
  }

  @Override
  public DockerImage image(TorLayerConfiguration layerConfiguration) {
    return IMAGE;
  }

}

package com.github.yassine.soxychains.subsystem.layer.spi.tor;


import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;

import java.net.URI;

@AutoService(LayerProvider.class)
public class TorLayerProvider implements LayerProvider<TorLayerConfiguration, TorNodeConfiguration> {

  private static final DockerImage IMAGE = new DockerImage("tor-node", URI.create("classpath://com/github/yassine/soxychains/subsystem/layer/spi/tor"), ImmutableMap.of());

  @Override
  public void configureNode(CreateContainerCmd containerCmd, TorNodeConfiguration nodeConfiguration, TorLayerConfiguration layerConfiguration) {

  }

  @Override
  public boolean matches(Container container, TorNodeConfiguration nodeConfiguration, TorLayerConfiguration layerConfiguration) {
    return true;
  }

  @Override
  public DockerImage image() {
    return IMAGE;
  }

}

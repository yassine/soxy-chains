package com.github.yassine.soxychains.subsystem.layer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;

public interface LayerProvider<CONFIGURATION extends AbstractLayerConfiguration, NODE_CONFIGURATION extends LayerNodeConfiguration> {
  void configureNode(CreateContainerCmd containerCmd, NODE_CONFIGURATION nodeConfiguration, CONFIGURATION layerConfiguration);
  boolean matches(Container container, NODE_CONFIGURATION nodeConfiguration, CONFIGURATION layerConfiguration);
  DockerImage image(CONFIGURATION layerConfiguration);
}

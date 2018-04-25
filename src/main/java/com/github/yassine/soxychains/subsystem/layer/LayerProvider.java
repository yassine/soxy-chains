package com.github.yassine.soxychains.subsystem.layer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;

public interface LayerProvider<L extends AbstractLayerContext, N extends LayerNodeConfiguration> {
  void configureNode(CreateContainerCmd containerCmd, N nodeConfiguration, L layerConfiguration);
  DockerImage image(L layerConfiguration);
}

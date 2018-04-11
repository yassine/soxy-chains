package com.github.yassine.soxychains.subsystem.layer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.yassine.soxychains.subsystem.layer.node.LayerNodeConfiguration;

public interface LayerService<CONFIGURATION extends AbstractLayerConfiguration, NODE_CONFIGURATION extends LayerNodeConfiguration> {
  void configureNode(CreateContainerCmd containerCmd, NODE_CONFIGURATION node_configuration);
}

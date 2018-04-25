package com.github.yassine.soxychains.subsystem.layer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class LayerNode {
  private final int layerIndex;
  private final AbstractLayerContext layerConfiguration;
  private final LayerNodeConfiguration nodeConfiguration;
}

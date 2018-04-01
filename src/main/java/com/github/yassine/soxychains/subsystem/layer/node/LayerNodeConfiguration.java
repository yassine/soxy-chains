package com.github.yassine.soxychains.subsystem.layer.node;

import java.io.Serializable;

public interface LayerNodeConfiguration extends Serializable{
  String name();
  int layerIndex();
}

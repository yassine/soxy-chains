package com.github.yassine.soxychains.subsystem.layer;

import com.github.dockerjava.api.model.Network;
import io.reactivex.Maybe;

public interface LayerObserver {
  Maybe<Boolean> onLayerAdd(Integer index, AbstractLayerContext layerConfiguration, Network network);
  Maybe<Boolean> onLayerPreRemove(Integer index, AbstractLayerContext layerConfiguration, Network network);
}

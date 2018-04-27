package com.github.yassine.soxychains.subsystem.layer;

import io.reactivex.Single;

public interface LayerService {
  Single<Boolean> add(LayerNode node);
  Single<Boolean> remove(LayerNode node);
  Single<Boolean> addLayer(int index, AbstractLayerConfiguration layerConfiguration);
  Single<Boolean> removeLayer(int index, AbstractLayerConfiguration layerConfiguration);
}

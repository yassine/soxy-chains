package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerContext;
import io.reactivex.Single;

public interface Gobetween {
  Single<Boolean> register(int layerIndex, AbstractLayerContext layerConfiguration);
  Single<Boolean> unRegister(int layerIndex, AbstractLayerContext layerConfiguration);
  DockerHostConfiguration host();
}

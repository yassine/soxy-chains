package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import io.reactivex.Single;

public interface Gobetween {
  Single<Boolean> register(int layerIndex, AbstractLayerConfiguration layerConfiguration);
  Single<Boolean> unRegister(int layerIndex, AbstractLayerConfiguration layerConfiguration);
  DockerHostConfiguration host();
}

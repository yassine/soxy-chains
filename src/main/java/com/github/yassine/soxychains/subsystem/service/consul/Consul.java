package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.layer.LayerNode;
import io.reactivex.Single;

public interface Consul {
  Single<Boolean> register(LayerNode layerNode);
  Single<Boolean> unRegister(LayerNode layerNode);
  DockerHostConfiguration host();
}

package com.github.yassine.soxychains.subsystem.layer;

import io.reactivex.Single;

public interface LayerService {
  Single<Boolean> add(LayerNode node);
  Single<Boolean> remove(LayerNode node);
}

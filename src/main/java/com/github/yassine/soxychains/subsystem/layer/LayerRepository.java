package com.github.yassine.soxychains.subsystem.layer;

import com.github.yassine.soxychains.subsystem.docker.client.HostManager;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class LayerRepository {

  private final HostManager hostManager;

}

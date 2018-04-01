package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.model.Container;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(staticName = "of") @Getter @Accessors(fluent = true)
class CreateContainerStatus {
  private final Container container;
  private final String message;
  private final boolean isStarted;
}

package com.github.yassine.soxychains.subsystem.layer.spi.tor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerContext;
import lombok.Getter;

import java.util.Set;

@JsonTypeName("tor") @Getter
public class TorLayerContext extends AbstractLayerContext {
  private Set<String> excludeExitNodes;
  private Set<String> excludeNodes;
  private Set<String> entryNodes;
  private Set<String> exitNodes;
}

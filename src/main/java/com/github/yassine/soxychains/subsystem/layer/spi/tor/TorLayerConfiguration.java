package com.github.yassine.soxychains.subsystem.layer.spi.tor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import lombok.Getter;

import java.util.Set;

@JsonTypeName("tor") @Getter
public class TorLayerConfiguration extends AbstractLayerConfiguration{
  private Set<String> excludeExitNodes;
  private Set<String> excludeNodes;
  private Set<String> entryNodes;
  private Set<String> exitNodes;
}

package com.github.yassine.soxychains.subsystem.layer.spi.tor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import lombok.Getter;

@JsonTypeName("tor") @Getter
public class TorLayerConfiguration extends AbstractLayerConfiguration{
  private String excludeExitNodes;
  private String excludeNodes;
  private String entryNodes;
  private String exitNodes;
}

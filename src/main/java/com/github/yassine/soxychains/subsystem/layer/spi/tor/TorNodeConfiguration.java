package com.github.yassine.soxychains.subsystem.layer.spi.tor;

import com.github.yassine.soxychains.subsystem.layer.LayerNodeConfiguration;
import lombok.Getter;

@Getter
public class TorNodeConfiguration implements LayerNodeConfiguration{
  private String excludeExitNodes;
  private String excludeNodes;
  private String entryNodes;
  private String exitNodes;
}

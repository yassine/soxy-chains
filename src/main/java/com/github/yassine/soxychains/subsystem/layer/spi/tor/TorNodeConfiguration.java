package com.github.yassine.soxychains.subsystem.layer.spi.tor;

import com.github.yassine.soxychains.subsystem.layer.LayerNodeConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter @Setter @Accessors(chain = true)
@EqualsAndHashCode(of = {"excludeExitNodes","excludeNodes", "entryNodes", "exitNodes"}, callSuper = false)
public class TorNodeConfiguration implements LayerNodeConfiguration{
  private Set<String> excludeExitNodes;
  private Set<String> excludeNodes;
  private Set<String> entryNodes;
  private Set<String> exitNodes;
}

package com.github.yassine.soxychains.subsystem.layer.spi.ovpn;

import com.github.yassine.soxychains.subsystem.layer.LayerNodeConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(of = "configuration") @Setter @Accessors(chain = true)
public class OpenVPNNodeConfiguration implements LayerNodeConfiguration {
  @NotNull @Getter @Setter
  private OpenVPNConfiguration configuration;
}

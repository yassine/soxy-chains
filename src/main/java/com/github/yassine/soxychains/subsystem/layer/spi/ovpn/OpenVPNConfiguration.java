package com.github.yassine.soxychains.subsystem.layer.spi.ovpn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.security.cert.X509Certificate;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter @EqualsAndHashCode(of = {"address", "port"}) @Setter @Accessors(chain = true)
public class OpenVPNConfiguration implements Serializable {
  private String user;
  private String password;
  @NotNull
  private String address;
  @NotNull
  private TransportProtocol protocol;
  @NotNull
  private Integer port;
  private Integer bandwidth;
  private transient X509Certificate certificate;
  @NotNull
  private String base64Configuration;
}

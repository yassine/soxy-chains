package com.github.yassine.soxychains.subsystem.docker.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yassine.soxychains.subsystem.docker.config.validation.ValidTLSConfigConstraint;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.URI;

@Getter @ValidTLSConfigConstraint @EqualsAndHashCode(of = "uri")
public class DockerHostConfiguration {
  private static final String UNIX = "unix";
  @NotNull @Setter(AccessLevel.PACKAGE)
  private URI uri;
  @NotNull @JsonProperty("usesTLS")
  private Boolean usesTLS = false;
  private String certPath;

  @SneakyThrows
  public String getHostname(){
    return uri.getScheme().equalsIgnoreCase(UNIX) ? InetAddress.getLocalHost().getHostAddress()
                                                  : uri.getHost();
  }

}

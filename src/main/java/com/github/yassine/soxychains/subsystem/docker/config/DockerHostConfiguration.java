package com.github.yassine.soxychains.subsystem.docker.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yassine.soxychains.subsystem.docker.config.validation.ValidTLSConfigConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.net.URI;

@Getter @ValidTLSConfigConstraint @EqualsAndHashCode(of = "uri")
public class DockerHostConfiguration {
  private static String LOCALHOST = "127.0.0.1";
  private static String UNIX = "unix";
  @NotNull
  private URI uri;
  @NotNull @JsonProperty("usesTLS")
  private Boolean usesTLS = false;
  private String certPath;

  public String getHostname(){
    return uri.getScheme().equalsIgnoreCase(UNIX) ? LOCALHOST
                                                  : uri.getHost();
  }

}

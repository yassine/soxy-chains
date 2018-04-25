package com.github.yassine.soxychains.subsystem.docker.networking;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DNSConfiguration {
  public static final String DNS_CONFIG_ID = "dns";
  @NotNull
  private String image = DNS_CONFIG_ID;
  @NotNull
  private String  serviceName    = DNS_CONFIG_ID;
  @NotNull
  private Integer managementPort = 8070;
}

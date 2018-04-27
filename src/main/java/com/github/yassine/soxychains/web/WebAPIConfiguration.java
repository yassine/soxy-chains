package com.github.yassine.soxychains.web;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.validation.constraints.NotNull;

import static java.net.InetAddress.getLocalHost;

@Getter
public class WebAPIConfiguration {

  @NotNull
  private String bindAddress = localAddress();
  @NotNull
  private Integer port = 9000;

  @SneakyThrows
  private static String localAddress() {
    return getLocalHost().getHostAddress();
  }

}

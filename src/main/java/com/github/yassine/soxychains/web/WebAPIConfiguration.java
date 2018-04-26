package com.github.yassine.soxychains.web;

import lombok.Getter;
import lombok.SneakyThrows;

import static java.net.InetAddress.getLocalHost;

@Getter
public class WebAPIConfiguration {

  private String  bindAddress = localAddress();
  private Integer port = 9000;

  @SneakyThrows
  private static String localAddress() {
    return getLocalHost().getHostAddress();
  }

}

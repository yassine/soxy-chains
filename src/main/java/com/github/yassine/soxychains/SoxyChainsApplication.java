package com.github.yassine.soxychains;

import com.github.yassine.soxychains.cli.ConfigurableCommand;

public class SoxyChainsApplication {
  public static void main(String... args){
    new SoxyChainsCLI.Builder()
      .withCommandsPackage(ConfigurableCommand.class.getPackage())
      .build()
      .run(args);
  }
}

package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.cli.command.ConfigurableCommand;

public class Application {

  private Application(){}

  public static void main(String... args){
    new SoxyChainsCLI.Builder()
      .withCommandsPackage(ConfigurableCommand.class.getPackage())
      .build()
      .run(args);
  }
}

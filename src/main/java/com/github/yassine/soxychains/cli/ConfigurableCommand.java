package com.github.yassine.soxychains.cli;

import io.airlift.airline.Option;
import lombok.Getter;

@Getter
public abstract class ConfigurableCommand implements Runnable {
  @Option(name = "-c", description = "The path to the configuration file")
  public String configPath;
}

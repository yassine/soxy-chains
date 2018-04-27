package com.github.yassine.soxychains.cli.command;

import com.github.yassine.soxychains.web.WebAPI;
import com.github.yassine.soxychains.web.WebAPIModule;
import com.google.inject.Inject;
import io.airlift.airline.Command;

@RequiresExtraModule(WebAPIModule.class)
@Command(name = "serve", description = "launches the api web server.")
public class ServeCommand extends ConfigurableCommand {

  @Inject
  private WebAPI webAPI;

  @Override
  public void run() {
    webAPI.startup();
  }
}

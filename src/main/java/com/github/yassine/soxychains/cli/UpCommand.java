package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.PhaseRunner;
import com.google.inject.Inject;
import io.airlift.airline.Command;

@Command(name = "up", description = "bootstrap the platform services")
public class UpCommand extends ConfigurableCommand{

  @Inject
  private PhaseRunner phaseRunner;

  @Override
  public void run() {
    //make sure infrastructure requirements are met
    phaseRunner.runPhase(Phase.INSTALL);
    //bootstrap the services
    phaseRunner.runPhase(Phase.START);
  }

}

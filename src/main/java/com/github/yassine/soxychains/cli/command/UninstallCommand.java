package com.github.yassine.soxychains.cli.command;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.PhaseRunner;
import com.google.inject.Inject;
import io.airlift.airline.Command;

@Command(name = "uninstall", description = "uninstall the infrastructure requirements")
public class UninstallCommand extends ConfigurableCommand{

  @Inject
  private PhaseRunner phaseRunner;

  @Override
  public void run() {
    phaseRunner.runPhase(Phase.STOP).blockingGet();
    phaseRunner.runPhase(Phase.UNINSTALL).blockingGet();
  }

}

package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.PhaseRunner;
import com.google.inject.Inject;
import io.airlift.airline.Command;

@Command(name = "install", description = "installs infrastructure requirements (images, networks etc.)")
public class InstallCommand extends ConfigurableCommand{

  @Inject
  private PhaseRunner phaseRunner;

  @Override
  public void run() {
    phaseRunner.runPhase(Phase.INSTALL).blockingGet();
  }

}

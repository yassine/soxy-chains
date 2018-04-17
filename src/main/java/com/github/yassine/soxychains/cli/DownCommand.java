package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.PhaseRunner;
import com.google.inject.Inject;
import io.airlift.airline.Command;

@Command(name = "down", description = "stops services")
public class DownCommand extends ConfigurableCommand{

  @Inject
  private PhaseRunner phaseRunner;

  @Override
  public void run() {
    phaseRunner.runPhase(Phase.STOP).blockingGet();
  }

}

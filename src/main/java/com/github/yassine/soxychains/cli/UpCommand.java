package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.PhaseRunner;
import com.google.inject.Inject;
import io.airlift.airline.Command;
import lombok.extern.slf4j.Slf4j;

@Command(name = "up", description = "bootstrap the platform services") @Slf4j
public class UpCommand extends ConfigurableCommand{

  @Inject
  private PhaseRunner phaseRunner;

  @Override
  public void run() {
    //make sure infrastructure requirements are met
    Boolean success = phaseRunner.runPhase(Phase.INSTALL).blockingGet();
    if(success){
      //bootstrap the services
      phaseRunner.runPhase(Phase.START).blockingGet();
    }else{
      log.info("Phase install exited with error. Skipping Start phase.");
    }
  }

}

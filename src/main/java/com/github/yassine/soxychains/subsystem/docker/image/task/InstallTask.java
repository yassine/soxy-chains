package com.github.yassine.soxychains.subsystem.docker.image.task;

import com.github.yassine.soxychains.SoxyChainsConfiguration;
import com.google.inject.Inject;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The install task would make sure that all the required docker images (network drivers, services, layer nodes)
 * are available in the hosts of the cluster.
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class InstallTask {

  private final SoxyChainsConfiguration configuration;


  private Observable<?> getNecessaryImages(){
    return null;
  }
}

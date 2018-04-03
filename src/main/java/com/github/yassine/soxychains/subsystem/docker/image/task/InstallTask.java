package com.github.yassine.soxychains.subsystem.docker.image.task;

import io.reactivex.Observable;

/**
 * The install task would make sure that all the required docker images (network drivers, services, layer nodes)
 * are available in the hosts of the cluster.
 */
public class InstallTask {
  private Observable<?> getNecessaryImages(){
    return null;
  }
}

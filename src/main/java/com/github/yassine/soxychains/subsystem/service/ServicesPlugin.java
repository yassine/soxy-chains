package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.image.config.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.config.FloatingDockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.config.RemoteDockerImage;

import java.net.URI;
import java.util.Optional;

/**
 * The main contract to fulfill for a given Service.
 * - Services typically run as container (not exclusively) on each host
 * - Services may declare dependencies through '@DependsOn' annotation from the 'guice-utils' module in order to require
 * another service to start before booting.
 *
 * (WIP: Spec may evolve yet)
 *
 * @param <CONFIG>
 */
public interface ServicesPlugin<CONFIG extends ServicesPluginConfiguration> extends Plugin<CONFIG> {

  /**
   * A service would typically run as a container on a given host. If so, it should
   * @param config
   * @return
   */
  default Optional<DockerImage> getImage(CONFIG config){
    String path = "classpath://"+getClass().getPackage().getName().replaceAll("\\.","/");
    if (getClass().getResourceAsStream("Dockerfile") == null &&  getClass().getResourceAsStream("Dockerfile.template") == null){
      return Optional.of(new RemoteDockerImage(config.imageName()));
    }
    return Optional.of(new FloatingDockerImage(config.imageName(), URI.create(path)));
  }

}

package com.github.yassine.soxychains.subsystem.service;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import io.reactivex.Single;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dockerjava.api.model.ExposedPort.tcp;
import static com.github.dockerjava.api.model.ExposedPort.udp;
import static com.github.dockerjava.api.model.PortBinding.parse;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;


/**
 * The main contract to fulfill for a given Service.
 * - Services typically run as container (not exclusively) on each host
 * - Services may declare dependencies through '@DependsOn' annotation from the 'guice-utils' module in order to require
 * another service to start before booting.
 * - Services requiring a custom docker image (one that needs to be built as part of the soxy-chains environment) can use
 * the '@RequiresImage' annotation
 *
 * (WIP: Spec may evolve yet)
 *
 * @param <CONFIG>
 */
public interface ServicesPlugin<CONFIG extends ServicesPluginConfiguration> extends Plugin<CONFIG> {

  default void configureContainer(CreateContainerCmd createContainerCmd, CONFIG pluginConfiguration, DockerConfiguration dockerConfiguration){
    ofNullable(pluginConfiguration.servicePorts())
      .ifPresent(servicePorts ->
        createContainerCmd
          .withExposedPorts(
            servicePorts.stream()
              .flatMap(servicePort -> Stream.of(tcp(servicePort), udp(servicePort)))
              .collect(Collectors.toList())
          )
          .withPortBindings(
            servicePorts.stream()
              .map(servicePort -> parse(format("%s:%s", servicePort, servicePort)))
              .collect(Collectors.toList())
          )
      );
  }

  default Single<Boolean> isReady(DockerHostConfiguration host, CONFIG config){
    return Single.just(true);
  }

}

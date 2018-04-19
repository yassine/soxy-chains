package com.github.yassine.soxychains.subsystem.service.tower;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulService;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;

@DependsOn(ConsulService.class) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class TowerService implements ServicesPlugin<TowerServiceConfiguration> {

  private final ConsulConfiguration consulConfiguration;
  @Override
  public void configureContainer(CreateContainerCmd createContainerCmd, TowerServiceConfiguration pluginConfiguration, DockerConfiguration dockerConfiguration) {
    createContainerCmd
      .withEnv(
        "CONSUL_HOST="+nameSpaceContainer(dockerConfiguration, consulConfiguration.serviceName()),
        "CONSUL_PORT="+consulConfiguration.getServicePort(),
        "SERVICE_KEY_LABEL="+LAYER_SERVICE_KEY_LABEL,
        "NODE_LABEL="+LAYER_NODE_LABEL,
        "NAMESPACE_KEY="+NAMESPACE_LABEL,
        "NAMESPACE="+dockerConfiguration.getNamespace()
      );
  }
}

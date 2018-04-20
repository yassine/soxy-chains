package com.github.yassine.soxychains.subsystem.service.tower;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;

@AutoService(ServicesPlugin.class)
@RequiresImage(name = TowerServiceConfiguration.NAME, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+TowerServiceConfiguration.NAME)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class TowerService implements ServicesPlugin<TowerServiceConfiguration> {

  private final ConsulConfiguration consulConfiguration;
  @Override
  public void configureContainer(CreateContainerCmd createContainerCmd, TowerServiceConfiguration pluginConfiguration, DockerConfiguration dockerConfiguration) {
    createContainerCmd.withBinds(
        Bind.parse("/var/run/docker.sock:/var/run/docker.sock")
      )
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

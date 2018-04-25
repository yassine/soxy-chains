package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dockerjava.api.model.ExposedPort.tcp;
import static com.github.dockerjava.api.model.ExposedPort.udp;
import static com.github.dockerjava.api.model.PortBinding.parse;
import static com.github.yassine.soxychains.core.FluentUtils.getWithRetry;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.CONSUL_CONFIG_ID;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@RequiresImage(name = CONSUL_CONFIG_ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+ CONSUL_CONFIG_ID)
@AutoService(ServicesPlugin.class) @ConfigKey(CONSUL_CONFIG_ID)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ConsulService implements ServicesPlugin<ConsulConfiguration>{

  private final ConsulProvider consulProvider;

  @Override
  public void configureContainer(CreateContainerCmd createContainerCmd, ConsulConfiguration pluginConfiguration, DockerContext dockerContext) {
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
    createContainerCmd.withBinds(
      Bind.parse("/var/run/docker.sock:/var/run/docker.sock")
    );
  }

  @Override
  public Single<Boolean> isReady(DockerHostConfiguration host, ConsulConfiguration consulConfiguration) {
    return getWithRetry( () -> consulProvider.get(host).setKVValue(ConsulService.class.getName(), "Ready" ).getValue(),
        retry -> format("Successfully got consul up at host '%s' after %s retry(ies).", host.getHostname(), retry),
        retry -> format("Failed at getting consul up at host '%s' after %s retry(ies).", host.getHostname(), retry))
      .toSingle(false);
  }

}

package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.core.FluentUtils.getWithRetry;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.CONSUL_CONFIG_ID;
import static java.lang.String.format;

@RequiresImage(name = CONSUL_CONFIG_ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+ CONSUL_CONFIG_ID)
@AutoService(ServicesPlugin.class) @ConfigKey(CONSUL_CONFIG_ID)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ConsulService implements ServicesPlugin<ConsulConfiguration>{

  private final ConsulProvider consulProvider;

  @Override
  public Single<Boolean> isReady(DockerHostConfiguration host, ConsulConfiguration consulConfiguration) {
    return getWithRetry( () -> consulProvider.get(host).setKVValue(ConsulService.class.getName(), "Ready" ).getValue(),
        (retry) -> format("Successfully got consul up at host '%s' after %s retry(ies).", host.getHostname(), retry),
        (retry) -> format("Failed at getting consul up at host '%s' after %s retry(ies).", host.getHostname(), retry))
      .toSingle(false);
  }

}

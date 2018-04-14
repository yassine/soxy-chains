package com.github.yassine.soxychains.subsystem.docker.networking;

import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.SOXY_DRIVER_NAME;

@AutoService(ImageRequirer.class) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class NetworkingImageRequirer implements ImageRequirer{
  private final DockerConfiguration dockerConfiguration;
  @Override
  public Observable<DockerImage> require() {
    return Observable.just(new DockerImage(SOXY_DRIVER_NAME,
      URI.create(Joiner.on("/").join("classpath:/", getClass().getPackage().getName().replaceAll("\\.","/"), "soxy_driver")),
      ImmutableMap.of("config", dockerConfiguration)
    ));
  }
}

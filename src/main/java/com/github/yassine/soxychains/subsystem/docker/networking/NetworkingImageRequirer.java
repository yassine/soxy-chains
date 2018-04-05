package com.github.yassine.soxychains.subsystem.docker.networking;

import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;

import java.net.URI;

@AutoService(ImageRequirer.class)
public class NetworkingImageRequirer implements ImageRequirer{
  static final String IMAGE_NAME = "soxy-driver";
  @Override
  public Observable<DockerImage> require() {
    return Observable.just(new DockerImage("soxy-driver",
      URI.create(Joiner.on("/").join(getClass().getPackage().getName().replaceAll("\\.","/"), IMAGE_NAME.replaceAll("-","_"))),
      ImmutableMap.of()
    ));
  }
}

package com.github.yassine.soxychains.subsystem.docker.image.api;

import io.reactivex.Observable;

public interface ImageRequirer {
  Observable<DockerImage> require();
}

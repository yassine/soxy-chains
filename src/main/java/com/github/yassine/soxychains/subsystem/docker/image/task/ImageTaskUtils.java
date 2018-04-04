package com.github.yassine.soxychains.subsystem.docker.image.task;

import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import io.reactivex.Observable;

import java.util.HashSet;
import java.util.Set;

public class ImageTaskUtils {

  public static Observable<DockerImage> getNecessaryImages(Set<ImageRequirer> imageRequirer) {
    return Observable.fromIterable(imageRequirer)
      .flatMap(ImageRequirer::require)
      .collectInto(new HashSet<DockerImage>(), HashSet::add).toObservable()
      .flatMap(Observable::fromIterable);
  }

}

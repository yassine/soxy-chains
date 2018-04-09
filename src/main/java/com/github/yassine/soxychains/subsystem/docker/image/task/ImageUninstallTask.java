package com.github.yassine.soxychains.subsystem.docker.image.task;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceImage;
import static com.github.yassine.soxychains.subsystem.docker.image.task.ImageTaskUtils.getNecessaryImages;

/**
 *
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
@RunOn(Phase.UNINSTALL) @AutoService(Task.class)
public class ImageUninstallTask implements Task{

  private final Set<ImageRequirer> imageRequirer;
  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;

  @Override
  public Single<Boolean> execute() {
    return Observable.fromIterable(dockerProvider.dockers())
      .flatMap(docker -> getNecessaryImages(imageRequirer)
        .flatMapMaybe(image -> docker.removeImage(nameSpaceImage(dockerConfiguration, image.getName()),
                                                  imgCmd -> {},
                                                  imageID -> {})
          .defaultIfEmpty(false)))
      .reduce(true , (a,b) -> a && b);
  }

}

package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import io.reactivex.Maybe;

import java.util.function.Consumer;

public interface Docker {
  Maybe<Image> findImageByTag(String imageTag);

  Maybe<Boolean> removeImage(String tag,
                             Consumer<RemoveImageCmd> beforeRemove,
                             Consumer<String> afterRemove);
  Maybe<String> buildImage(String tag,
                           Consumer<BuildImageCmd> beforeCreate,
                           Consumer<String> afterCreate);
  Maybe<String> createNetwork(String networkName,
                              Consumer<CreateNetworkCmd> beforeCreate,
                              Consumer<String> afterCreate);
  Maybe<Boolean> removeNetwork(String networkName,
                               Consumer<RemoveNetworkCmd> beforeRemove,
                               Consumer<String> afterRemove);
  Maybe<Container> startContainer(String containerName,
                                  String image,
                                  Consumer<CreateContainerCmd> beforeCreate,
                                  Consumer<String> afterCreate,
                                  Consumer<StartContainerCmd> beforeStart,
                                  Consumer<String> afterStart);
  Maybe<Boolean> stopContainer(String containerName,
                               Consumer<StopContainerCmd> beforeStop,
                               Consumer<String> afterStop,
                               Consumer<RemoveContainerCmd> beforeRemove,
                               Consumer<String> afterRemove);
  SoxyChainsDockerClient client();
}

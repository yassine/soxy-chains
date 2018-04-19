package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import io.reactivex.Maybe;

import java.util.function.Consumer;

public interface Docker {
  Maybe<Image> findImageByTag(String imageTag);

  Maybe<Boolean> removeImage(String tag,
                             Consumer<RemoveImageCmd> beforeRemove,
                             Consumer<String> afterRemove);

  Maybe<Boolean> removeImage(String tag);
  Maybe<String> buildImage(String tag,
                           Consumer<BuildImageCmd> beforeCreate,
                           Consumer<String> afterCreate);
  Maybe<String> buildImage(String tag,
                           Consumer<BuildImageCmd> beforeCreate);
  Maybe<String> createNetwork(String networkName,
                              Consumer<CreateNetworkCmd> beforeCreate,
                              Consumer<String> afterCreate);
  Maybe<String> createNetwork(String networkName,
                              Consumer<CreateNetworkCmd> beforeCreate);
  Maybe<Boolean> removeNetwork(String networkName,
                               Consumer<RemoveNetworkCmd> beforeRemove,
                               Consumer<String> afterRemove);
  Maybe<Boolean> removeNetwork(String networkName);
  Maybe<Network> findNetwork(String networkName);
  Maybe<Container> findContainer(String networkName);
  Maybe<Container> runContainer(String containerName,
                                String image,
                                Consumer<CreateContainerCmd> beforeCreate,
                                Consumer<String> afterCreate,
                                Consumer<StartContainerCmd> beforeStart,
                                Consumer<String> afterStart);
  Maybe<Container> runContainer(String containerName,
                                String image,
                                Consumer<CreateContainerCmd> beforeCreate);
  Maybe<Boolean> joinNetwork(String containerId, String network);
  Maybe<Boolean> leaveNetwork(String containerId, String networkName);
  Maybe<Boolean> stopContainer(String containerName,
                               Consumer<StopContainerCmd> beforeStop,
                               Consumer<String> afterStop,
                               Consumer<RemoveContainerCmd> beforeRemove,
                               Consumer<String> afterRemove);

  Maybe<Boolean> stopContainer(String containerName);
  DockerHostConfiguration hostConfiguration();
}

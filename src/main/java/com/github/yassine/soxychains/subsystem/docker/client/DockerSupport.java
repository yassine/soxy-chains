package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.yassine.soxychains.core.SoxyChainsException;
import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.github.yassine.soxychains.core.FluentUtils.runAndGet;
import static com.github.yassine.soxychains.core.FluentUtils.runAndGetAsMaybe;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.labelizeNamedEntity;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.reactivex.Maybe.fromFuture;
import static io.reactivex.Maybe.just;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;


@RequiredArgsConstructor @Slf4j @Accessors(fluent = true)
class DockerSupport implements Docker {
  private final SoxyChainsDockerClient client;
  private final DockerConfiguration dockerConfiguration;

  @Override
  public Maybe<Image> findImageByTag(String imageTag) {
    return fromFuture( supplyAsync( () ->
      client.listImagesCmd()
        .exec()
        .stream()
        .filter(image -> stream(ofNullable(image.getRepoTags()).orElse(new String[0]))
                          .anyMatch( repoTag-> !isNullOrEmpty(imageTag) && repoTag.contains(imageTag)))
        .findAny()
        .map(Maybe::just)
        .orElse(Maybe.empty())
    ) )
    .flatMap(m -> m).subscribeOn(Schedulers.io());
  }

  @Override
  public Maybe<String> createNetwork(String networkName, Consumer<CreateNetworkCmd> beforeCreate, Consumer<String> afterCreate) {
    return findNetwork(networkName)
      .map(Network::getId)
      .map(networkID -> runAndGet(() -> log.info(format("Network '%s' alreadyExists", networkName)), just(networkID)))
      .defaultIfEmpty( just(1)
        .flatMap( value -> new SyncDockerExecutor<>(client.createNetworkCmd(), client.configuration())
          .withSuccessFormatter( network -> format("Successfully created network '%s' to id '%s' ", networkName, network.getId()) )
          .withErrorFormatter( exception -> format("An occurred while creating network '%s' : '%s'", networkName, exception.getMessage()) )
          .withBeforeExecute( c -> {
              beforeCreate.accept(c);
              c.withName(networkName).withLabels(overrideLabels(c.getLabels(), labelizeNamedEntity(networkName, dockerConfiguration)));
          })
          .withAfterExecute( networkResponse -> Optional.ofNullable(afterCreate).ifPresent(hook -> hook.accept( networkResponse.getId() ) ))
          .execute()
          .map(CreateNetworkResponse::getId)
        )
      )
      .flatMap(v -> v);
  }

  @Override
  public Maybe<String> createNetwork(String networkName, Consumer<CreateNetworkCmd> beforeCreate) {
    return createNetwork(networkName, beforeCreate, null);
  }

  @Override
  public Maybe<Network> findNetwork(String networkName) {
    return fromFuture(CompletableFuture.supplyAsync(() ->
      client.listNetworksCmd()
        .exec().stream()
        .filter(network -> network.getName().equals(networkName))
        .findAny()
        .map(Maybe::just)
        .orElse(Maybe.empty()).subscribeOn(Schedulers.io())
    ))
    .flatMap(v -> v);
  }

  @Override
  public Maybe<Boolean> removeNetwork(String networkName, Consumer<RemoveNetworkCmd> beforeRemove, Consumer<String> afterRemove) {
    return fromFuture(supplyAsync(() ->
      client.listNetworksCmd().exec().stream()
        .filter(network -> network.getName().equals(networkName))
        .findAny()
        .map(network -> just(1)
          .flatMap(v -> new SyncDockerExecutor<>(client.removeNetworkCmd(network.getId()), client.configuration() )
            .withSuccessFormatter( vd -> format("Successfully removed network '%s' with id '%s' ", networkName, network.getId()) )
            .withErrorFormatter( exception -> format("An occurred while removing network '%s' : '%s'", networkName, exception.getMessage()) )
            .withBeforeExecute(beforeRemove)
            .withAfterExecute( vd -> ofNullable(afterRemove).ifPresent(hook -> hook.accept( networkName )) )
            .execute()
            .subscribeOn(Schedulers.io()))
          .map(v -> true)
        )
        .orElseGet(() -> runAndGet(() -> log.info(format("Cannot remove network '%s'. The network doesn't exist", networkName)), just(false)))
    ))
    .flatMap(v -> v);
  }

  @Override
  public Maybe<Boolean> removeNetwork(String networkName) {
    return removeNetwork(networkName, null, null);
  }

  @Override
  public Maybe<Container> findContainer(String containerName) {
    return fromFuture(supplyAsync(() -> client.listContainersCmd()
      .withShowAll(true)
      .withLabelFilter(
        ImmutableMap.of(
          NamespaceUtils.SYSTEM_LABEL, "",
          NamespaceUtils.NAMESPACE_LABEL, dockerConfiguration.getNamespace()
        )
      )
      .exec()
      .stream()
      .filter(container1 -> Arrays.stream(container1.getNames()).anyMatch(name -> name.contains(containerName)))
      .findAny()
      .map(Maybe::just)
      .orElseGet(Maybe::empty)
    )).flatMap(v -> v);
  }

  @Override
  public Maybe<Container> runContainer(String containerName, String image, Consumer<CreateContainerCmd> beforeCreate, Consumer<String> afterCreate, Consumer<StartContainerCmd> beforeStart, Consumer<String> afterStart) {
    return fromFuture( supplyAsync( () -> {
        try{
          return findContainer(containerName).map(Optional::of).defaultIfEmpty(Optional.empty()).blockingGet()
            .map(
              container -> CreateContainerStatus.of(
                container,
                format("Skipping container '%s' creation. The container already exists.", containerName),
                container.getStatus() != null && container.getStatus().contains("Up")
              )
            )
            .orElseGet( () -> {
              CreateContainerCmd command = client.createContainerCmd(image)
                .withName(containerName);
              ofNullable(beforeCreate).ifPresent(hook -> hook.accept(command));
              String containerID = command.exec().getId();
              ofNullable(afterCreate).ifPresent(hook -> hook.accept(containerID));
              Container container = client.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(
                  ImmutableMap.of(
                    NamespaceUtils.SYSTEM_LABEL, "",
                    NamespaceUtils.NAMESPACE_LABEL, dockerConfiguration.getNamespace()
                  )
                )
                .exec()
                .stream()
                .filter(container1 -> Arrays.stream(container1.getNames()).anyMatch(name -> name.contains(containerName)))
                .findAny().orElseThrow(() -> new SoxyChainsException(format("Unable to find container with name : %s", containerName)));
              return CreateContainerStatus.of(container, format("Successfully created container '%s' with id '%s'.", containerName, containerID), false);
            });
        }catch (Exception e){
          log.error(format("Couldn't create container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return CreateContainerStatus.of(null, format("Couldn't create container '%s' : %s", containerName, e.getMessage()), false);
        }
      })
    ).flatMap(createContainerStatus -> {
      log.info(createContainerStatus.message());
      if(createContainerStatus.container() != null && !createContainerStatus.isStarted()){
        try{
          StartContainerCmd command = client.startContainerCmd(createContainerStatus.container().getId());
          ofNullable(beforeStart).ifPresent(hook -> hook.accept(command));
          command.exec();
          log.info("Successfully started container '{}'", Arrays.toString(createContainerStatus.container().getNames()));
          ofNullable(afterStart).ifPresent(hook -> hook.accept(containerName));
          return just(createContainerStatus.container());
        }catch (Exception e){
          log.error(format("Couldn't start container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return Maybe.empty();
        }
      }else if(createContainerStatus.container() != null && createContainerStatus.isStarted()){
        log.info(format("Skipping starting container '%s' as it is already started.", containerName));
        return just(createContainerStatus.container());
      }
      else{
        return Maybe.empty();
      }
    });
  }

  @Override
  public Maybe<Container> runContainer(String containerName, String image, Consumer<CreateContainerCmd> beforeCreate) {
    return runContainer(containerName, image, beforeCreate, null, null, null);
  }

  @Override
  public Maybe<Boolean> joinNetwork(Container container, String networkName) {
    return findNetwork(networkName)
      .flatMap(network -> new SyncDockerExecutor<>(client.connectToNetworkCmd()
                                                    .withContainerId(container.getId())
                                                    .withNetworkId(network.getId()), hostConfiguration())
                            .withSuccessFormatter(v -> format("Successfully made container '%s' join network '%s'", container.getNames()[0], network.getName()))
                            .withErrorFormatter(e -> format("Failed to make container '%s' join network '%s'", container.getNames()[0], network.getName()))
                            .execute())
      .map(Objects::nonNull);
  }


  @Override
  public Maybe<Boolean> leaveNetwork(String containerId, String networkName) {
    return findNetwork(networkName)
      .flatMap(network -> new SyncDockerExecutor<>(client.disconnectFromNetworkCmd()
        .withContainerId(containerId)
        .withNetworkId(network.getId()), hostConfiguration()).execute())
      .map(Objects::nonNull);
  }

  @Override
  public Maybe<Boolean> stopContainer(String containerName, Consumer<StopContainerCmd> beforeStop, Consumer<String> afterStop, Consumer<RemoveContainerCmd> beforeRemove, Consumer<String> afterRemove) {
    return fromFuture( supplyAsync( () -> {
        try{
          return findContainer(containerName).map(Optional::of).defaultIfEmpty(Optional.empty()).blockingGet()
            .map(container -> {
              if (container.getStatus().contains("Up")) {
                StopContainerCmd command = client.stopContainerCmd(container.getId());
                ofNullable(beforeStop).ifPresent(hook -> hook.accept(command));
                command.exec();
                log.info(format("Successfully stopped container '%s'.", containerName));
                ofNullable(afterStop).ifPresent(hook -> hook.accept(containerName));
              }else{
                log.info(format("Skipping container '%s' stopping. The container is already stopped.", containerName));
              }
              return Pair.of(container.getId(), true);
            })
            .orElseGet( () -> {
              log.info(format("Skipping container '%s' stopping/removal. The container doesn't exist.", containerName));
              return Pair.of(null, false);
            });
        }catch (Exception e){
          log.error(format("Couldn't stop container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return Pair.of( (String) null, false);
        }
      }
      )
    ).flatMap(pair -> {
      if(pair.getKey() != null){
        try{
          RemoveContainerCmd command = client.removeContainerCmd(pair.getKey());
          ofNullable(beforeRemove).ifPresent(hook -> hook.accept(command));
          command.exec();
          log.info(format("Successfully removed container '%s'.", containerName));
          ofNullable(afterRemove).ifPresent(hook -> hook.accept(containerName));
          return just(true);
        }catch (Exception e){
          log.error(format("Couldn't remove container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return just(false);
        }
      }else{
        return just(false);
      }
    }).subscribeOn(Schedulers.io());
  }

  @Override
  public Maybe<Boolean> stopContainer(String containerName) {
    return stopContainer(containerName, null, null, null, null);
  }

  @Override
  public DockerHostConfiguration hostConfiguration() {
    return client.configuration();
  }

  @Override
  public Maybe<Boolean> removeImage(String tag, Consumer<RemoveImageCmd> beforeRemove, Consumer<String> afterRemove) {
    return findImageByTag(tag)
      .flatMap(image ->
        runAndGetAsMaybe( () -> new SyncDockerExecutor<>(client.removeImageCmd(image.getId()).withForce(true), client.configuration() )
          .withSuccessFormatter( v -> format("Successfully removed image '%s'", tag) )
          .withErrorFormatter( exception -> format("An occurred before removing image '%s' : '%s'", tag, exception.getMessage()) )
          .withBeforeExecute( beforeRemove )
          .withAfterExecute( v -> ofNullable(afterRemove).ifPresent(hook -> hook.accept( tag )))
          .execute().blockingGet(), true).subscribeOn(Schedulers.io())
      ).defaultIfEmpty(true);
  }

  @Override
  public Maybe<Boolean> removeImage(String tag) {
    return removeImage(tag, null, null);
  }

  @Override
  public Maybe<String> buildImage(String tag, Consumer<BuildImageCmd> beforeCreate, Consumer<String> afterCreate){
    return findImageByTag(tag)
      .map(Optional::ofNullable)
      .defaultIfEmpty(Optional.empty())
      .flatMap(present -> present.map(Image::getId)
        .map(Maybe::just)
        .orElseGet(() -> just(1)
          .flatMap(v -> (this.<BuildImageCmd, BuildResponseItem, BuildImageResultCallback, String>getAsyncExecutor(client.buildImageCmd().withTags(ImmutableSet.of(tag)), client.configuration()))
            .withErrorFormatter( exception -> format("An error occurred before creating image '%s' : '%s'", tag, exception.getMessage()))
            .withSuccessFormatter( id -> format("Successfully created image '%s'. Image ID: '%s'", tag, id))
            .withResultExtractor(BuildImageResultCallback::awaitImageId)
            .withBeforeExecute(beforeCreate)
            .withAfterExecute(afterCreate)
            .withCallBack(new BuildImageResultCallback())
            .execute()
          )
        )
      )
      .subscribeOn(Schedulers.io());
  }

  @Override
  public Maybe<String> buildImage(String tag, Consumer<BuildImageCmd> beforeCreate) {
    return buildImage(tag, beforeCreate, null);
  }

  <CMD extends AsyncDockerCmd<CMD, ITEM>, ITEM, CALLBACK extends ResultCallback<ITEM>, RESULT> ASyncDockerExecutor<CMD, ITEM, CALLBACK, RESULT> getAsyncExecutor(CMD command, DockerHostConfiguration config){
    return new ASyncDockerExecutor<>(command, config);
  }

  private Map<String, String> overrideLabels(Map<String, String> userParams, Map<String, String> systemParams){
    return ImmutableMap.<String, String>builder()
      .putAll(ofNullable(userParams).orElse(ImmutableMap.of()))
      .putAll(ofNullable(systemParams).orElse(ImmutableMap.of()))
      .build();
  }

}

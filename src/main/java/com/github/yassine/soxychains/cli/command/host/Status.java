package com.github.yassine.soxychains.cli.command.host;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.cli.command.ConfigurableCommand;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.google.inject.Inject;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.github.yassine.soxychains.core.FluentUtils.runAndGet;
import static com.github.yassine.soxychains.core.FluentUtils.runAndGetAsSingle;

@Slf4j
@Command(name = "status", description = "displays the status of the docker hosts")
public class Status extends ConfigurableCommand{

  @Inject
  private DockerProvider dockerProvider;
  @Inject
  private ObjectMapper objectMapper;
  @Inject
  private SoxyChainsContext soxyChainsContext;

  @Option(name = "-o", description = "The path to the output file")
  public String outputPath;

  @Override
  public void run() {
    validateOptions();

    Observable.fromIterable(soxyChainsContext.getDocker().getHosts())
      .map(dockerHostConfiguration -> dockerProvider.getClient(dockerHostConfiguration))
      .flatMapSingle( client -> runAndGetAsSingle( () -> client.pingCmd().exec(), new HostStatus(client.configuration().getUri().getHost(), true) )
        .onErrorReturn( exception -> runAndGet(() -> log.error(exception.getMessage()), new HostStatus(client.configuration().getUri().getHost(), false)))
        .subscribeOn(Schedulers.io())
      ).collectInto(new ArrayList<>(), ArrayList::add)
    .toObservable().blockingSubscribe(statuses -> objectMapper.writeValue(outputStream(), statuses));
  }

  @SneakyThrows
  private OutputStream outputStream(){
    return outputPath == null ? System.out : new FileOutputStream(new File(outputPath));
  }

  private void validateOptions(){
    if(outputPath != null){
      File outputFile =(new File(outputPath));
      if(outputFile.exists() && outputFile.isDirectory()){
        log.warn("Output file {} is a directory. Using stdout instead as output", outputPath);
        outputPath = null;
      }
    }
  }

  @RequiredArgsConstructor @Getter
  public static class HostStatus {
    private final String host;
    private final boolean up;
  }

}

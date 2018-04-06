package com.github.yassine.soxychains.cli.host;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.cli.ConfigurableCommand;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.google.inject.Inject;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static io.reactivex.Observable.fromIterable;
import static io.reactivex.Single.fromFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Slf4j
@Command(name = "status", description = "displays the status of the docker hosts")
public class Status extends ConfigurableCommand{

  @Inject
  private DockerProvider dockerProvider;
  @Inject
  private ObjectMapper objectMapper;

  @Option(name = "-o", description = "The path to the output file")
  public String outputPath;

  @Override
  public void run() {
    validateOptions();
    fromIterable(dockerProvider.clients())
      .flatMapSingle(client -> fromFuture(supplyAsync(()->{
          try{
            client.pingCmd().exec();
            return new HostStatus(client.configuration().getUri().getHost(), true);
          }catch (Exception e){
            log.error(e.getMessage());
            return new HostStatus(client.configuration().getUri().getHost(), false);
          }
        }))
        .subscribeOn(Schedulers.io())
      ).collectInto(new ArrayList<>(), ArrayList::add)
    .toObservable().blockingSubscribe(statuses -> {
      objectMapper.writeValue(outputStream(), statuses);
    });
  }

  @SneakyThrows
  private OutputStream outputStream(){
    return outputPath == null ? System.out : new FileOutputStream(new File(outputPath));
  }

  private void validateOptions(){
    if(outputPath != null){
      File outputFile =(new File(outputPath));
      if(outputFile.exists() && outputFile.isDirectory()){
        System.out.println(String.format("Output file '%s' is a directory", outputPath));
        System.exit(1);
      }
    }
  }

  @RequiredArgsConstructor @Getter
  public static class HostStatus {
    private final String host;
    private final boolean up;
  }

}

package com.github.yassine.soxychains.subsystem.service.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.github.dockerjava.api.model.Network;
import com.github.yassine.soxychains.subsystem.docker.client.Docker;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.LayerObserver;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.core.FluentUtils.runAndGet;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.namespaceLayerService;
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.portShift;
import static io.reactivex.Observable.fromFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@AutoService(LayerObserver.class) @Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ConsulLayerObserver implements LayerObserver {

  private final DockerProvider dockerProvider;
  private final ConsulProvider consulProvider;
  private final ConsulConfiguration consulConfiguration;

  @Override
  public Maybe<Boolean> onLayerAdd(Integer index, AbstractLayerConfiguration layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .map(Docker::hostConfiguration)
      //register the cluster-wide service across all hosts
      .flatMap(dockerHost ->
        dockerProvider.dockers()
          .flatMap(docker -> fromFuture(supplyAsync(() -> {
            ConsulClient consul = consulProvider.get(docker.hostConfiguration());
            String host = dockerHost.getHostname();
            if(!consul.getCatalogServices(QueryParams.DEFAULT).getValue().containsKey(namespaceLayerService(index, ServiceScope.CLUSTER))
                || consul.getCatalogService(namespaceLayerService(index, ServiceScope.CLUSTER), QueryParams.DEFAULT)
                    .getValue().stream().map(CatalogService::getServiceId)
                    .noneMatch(serviceId -> serviceId.equals(formatServiceId(namespaceLayerService(index, ServiceScope.CLUSTER), host))))
            {
              NewService service = new NewService();
              NewService.Check serviceCheck = new NewService.Check();
              service.setName(namespaceLayerService(index, ServiceScope.CLUSTER));
              service.setAddress(host);
              service.setPort(portShift(index, layerConfiguration.getLocalServicePort()));
              service.setId(formatServiceId(namespaceLayerService(index, ServiceScope.CLUSTER), host));
              service.setTags(ImmutableList.of(service.getName()));
              service.setCheck(serviceCheck);
              serviceCheck.setScript(String.format("/consul/config/host-health-check.sh %s %s %s", host, consulConfiguration.getServicePort(), namespaceLayerService(index, ServiceScope.LOCAL)));
              serviceCheck.setInterval("15s");
              serviceCheck.setTimeout("15s");
              consul.agentServiceRegister(service);
            }
            return true;
          })).onErrorReturn(e-> runAndGet(()-> log.error(e.getMessage(), e), false)).subscribeOn(Schedulers.io()))
      ).reduce(true, AND_OPERATOR).toMaybe().subscribeOn(Schedulers.io());
  }

  @Override
  public Maybe<Boolean> onLayerPreRemove(Integer index, AbstractLayerConfiguration layerConfiguration, Network network) {
    return dockerProvider.dockers()
      .map(Docker::hostConfiguration)
      //register the cluster-wide service
      .flatMap(dockerHost ->
        dockerProvider.dockers()
          .flatMap(docker -> fromFuture(supplyAsync(() -> {
            String host = dockerHost.getHostname();
            ConsulClient consul = consulProvider.get(docker.hostConfiguration());
            if(consul.getCatalogServices(QueryParams.DEFAULT).getValue().containsKey(namespaceLayerService(index, ServiceScope.CLUSTER))
                && consul.getCatalogService(namespaceLayerService(index, ServiceScope.CLUSTER), QueryParams.DEFAULT)
                    .getValue().stream().map(CatalogService::getServiceId)
                    .anyMatch(serviceId -> serviceId.equals(formatServiceId(namespaceLayerService(index, ServiceScope.CLUSTER), host))))
            {
              consulProvider.get(docker.hostConfiguration()).agentServiceDeregister(formatServiceId(namespaceLayerService(index, ServiceScope.CLUSTER), host));
            }
            return true;
          })).onErrorReturn(e-> runAndGet(()-> log.error(e.getMessage(), e), false)).subscribeOn(Schedulers.io()))
      ).reduce(true, AND_OPERATOR).toMaybe().subscribeOn(Schedulers.io());
  }

  private String formatServiceId(String host, String serviceId){
    return String.format("%s#%s", host, serviceId);
  }

}

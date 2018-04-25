package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static com.github.dockerjava.core.DockerClientBuilder.getInstance;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DockerProviderSupport implements DockerProvider {

  private final DockerContext dockerContext;
  private final HostManager hostManager;

  private static final LoadingCache<DockerHostConfiguration, SoxyChainsDockerClient> clientCache = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, SoxyChainsDockerClient>() {
    @Override @SuppressWarnings("NullableProblems")
    public SoxyChainsDockerClient load(DockerHostConfiguration hostConfiguration){
      DefaultDockerClientConfig.Builder configBuilder = new DefaultDockerClientConfig.Builder();
      if(hostConfiguration.getUsesTLS()){
        configBuilder.withDockerTlsVerify(true);
        configBuilder.withDockerCertPath(hostConfiguration.getCertPath());
      }
      configBuilder.withDockerHost(hostConfiguration.getUri().toString());
      return new SoxyChainsDockerClientSupport(getInstance(configBuilder.build()).build(), hostConfiguration);
    }
  });

  private final LoadingCache<DockerHostConfiguration, Docker> dockerCache = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, Docker>() {
    @Override @SuppressWarnings("unchecked")
    public Docker load(DockerHostConfiguration key) throws Exception {
      return new DockerSupport(clientCache.get(key), dockerContext);
    }
  });

  @Override @SneakyThrows
  public Docker get(DockerHostConfiguration configuration) {
    return dockerCache.get(configuration);
  }

  @Override @SneakyThrows
  public SoxyChainsDockerClient getClient(DockerHostConfiguration configuration) {
    return clientCache.get(configuration);
  }

  @Override
  public Observable<Docker> dockers() {
    return hostManager.list().map(this::get);
  }

  @Override
  public Observable<SoxyChainsDockerClient> clients() {
    return hostManager.list().map(this::getClient);
  }
}

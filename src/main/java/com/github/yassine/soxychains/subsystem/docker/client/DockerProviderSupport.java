package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import static com.github.dockerjava.core.DockerClientBuilder.getInstance;

public class DockerProviderSupport implements DockerProvider {

  private static final LoadingCache<DockerHostConfiguration, SoxyChainsDockerClient> CLIENT_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, SoxyChainsDockerClient>() {
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

  private static final LoadingCache<DockerHostConfiguration, Docker> HELPER_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, Docker>() {
    @Override @SuppressWarnings("unchecked")
    public Docker load(DockerHostConfiguration key) throws Exception {
      return new DockerSupport(CLIENT_CACHE.get(key));
    }
  });

  @Override @SneakyThrows
  public Docker get(DockerHostConfiguration configuration) {
    return HELPER_CACHE.get(configuration);
  }

  @Override @SneakyThrows
  public SoxyChainsDockerClient getClient(DockerHostConfiguration configuration) {
    return CLIENT_CACHE.get(configuration);
  }
}

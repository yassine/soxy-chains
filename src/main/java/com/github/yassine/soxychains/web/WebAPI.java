package com.github.yassine.soxychains.web;

import com.github.yassine.soxychains.web.resource.HostResource;
import com.github.yassine.soxychains.web.resource.LayerResource;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.rapidoid.config.Conf;
import org.rapidoid.env.Env;
import org.rapidoid.setup.App;
import org.rapidoid.setup.AppBootstrap;
import org.rapidoid.setup.On;
import org.rapidoid.setup.Setup;

import static java.lang.String.format;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class WebAPI {

  private final WebAPIConfiguration configuration;
  private final LayerResource layerResource;
  private final HostResource hostResource;

  private final LoadingCache<String, AppBootstrap> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, AppBootstrap>() {
    @Override
    public AppBootstrap load(String s) throws Exception {
      String[] args = new String[]{
        format("on.port=%s", configuration.getPort()),
        format("on.address=%s", configuration.getBindAddress())
      };
      AppBootstrap bootstrap = App.bootstrap(args);
      On.get("/layer").json(layerResource::get);
      On.get("/host").json(hostResource::get);
      return bootstrap;
    }
  });

  @SneakyThrows
  public void startup(){
    cache.get("");
  }

  @SneakyThrows
  public void stop(){
    App.shutdown();
    Setup.shutdownAll();
    Env.reset();
    App.resetGlobalState();
    Conf.reset();
    Conf.ROOT.reset();
    cache.cleanUp();
  }

}

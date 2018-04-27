package com.github.yassine.soxychains.web;

import com.github.yassine.soxychains.SoxyChainsContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class WebAPIModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WebAPI.class).in(Singleton.class);
  }

  @Provides
  WebAPIConfiguration configuration(SoxyChainsContext soxyChainsContext){
    return soxyChainsContext.getApi();
  }

}

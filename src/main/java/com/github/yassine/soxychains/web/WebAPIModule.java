package com.github.yassine.soxychains.web;

import com.github.yassine.soxychains.SoxyChainsContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class WebAPIModule extends AbstractModule {
  @Provides
  WebAPIConfiguration configuration(SoxyChainsContext soxyChainsContext){
    return soxyChainsContext.getApi();
  }
}

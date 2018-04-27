package com.github.yassine.soxychains.web.resource;

import com.github.yassine.soxychains.ConfigurationModule;
import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.web.WebAPIModule;
import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
public class ResourceTestModule extends AbstractModule {

  @Override
  protected void configure() {
    InputStream is = getClass().getResourceAsStream("config-web.yaml");
    install(new ConfigurationModule(is));
    install(new SoxyChainsModule());
    install(new WebAPIModule());
  }

}

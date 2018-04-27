package com.github.yassine.soxychains.web.resource;

import com.github.yassine.soxychains.subsystem.docker.client.HostManager;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.GET;
import java.util.List;

@Singleton
public class HostResource {

  @Inject
  private HostManager hostManager;

  @GET
  public List<DockerHostConfiguration> get(){
    return hostManager.list().toList().blockingGet();
  }

}

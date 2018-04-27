package com.github.yassine.soxychains.web.resource;

import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Path("/layer") @Singleton
public class LayerResource {

  @Inject
  private SoxyChainsContext soxyChainsContext;

  @GET
  public List<AbstractLayerConfiguration> get(){
    return soxyChainsContext.getLayers();
  }

}

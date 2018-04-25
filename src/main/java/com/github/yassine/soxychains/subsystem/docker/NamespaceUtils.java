package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NamespaceUtils {

  private NamespaceUtils() {}

  private static final String SYSTEM_NAMESPACE    = "soxy_chains";
  private static final String IMAGE_SEPARATOR     = "__";
  private static final String CONTAINER_SEPARATOR = "__";
  private static final String NETWORK_SEPARATOR   = "__";
  public static final String SOXY_DRIVER_NAME    = "soxy-driver";
  public static final String SYSTEM_LABEL         = SoxyChainsModule.class.getPackage().getName();
  public static final String ORIGINAL_LABEL       = SYSTEM_LABEL+".original_name";
  public static final String NAMESPACE_LABEL      = SYSTEM_LABEL+".namespace";
  public static final String RANDOM_LABEL         = SYSTEM_LABEL+".random";
  public static final String LAYER_PROVIDER_LABEL = SYSTEM_LABEL+".layer.provider";
  public static final String LAYER_INDEX          = SYSTEM_LABEL+".layer.index";
  public static final String LAYER_SERVICE_KEY_LABEL = SYSTEM_LABEL+".layer.service_key";
  public static final String LAYER_NODE_LABEL     = SYSTEM_LABEL+".layer.node";

  public static String nameSpaceImage(DockerContext configuration, String userImageName){
    return userImageName.startsWith(Joiner.on(IMAGE_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userImageName
      : Joiner.on(IMAGE_SEPARATOR).join(userImageName, configuration.getNamespace(), SYSTEM_NAMESPACE);
  }

  public static String nameSpaceNetwork(DockerContext configuration, String userNetworkName){
    return userNetworkName.startsWith(Joiner.on(NETWORK_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userNetworkName
      : Joiner.on(NETWORK_SEPARATOR).join(userNetworkName, configuration.getNamespace(), SYSTEM_NAMESPACE);
  }

  public static String nameSpaceLayerNetwork(DockerContext configuration, int layerIndex){
    return nameSpaceNetwork(configuration, String.format("layer__%s", layerIndex));
  }

  public static String namespaceLayerNode(DockerContext configuration, int layerIndex, String name){
    return nameSpaceContainer(configuration, String.format("layer-%s__%s", layerIndex, name));
  }

  public static String nameSpaceContainer(DockerContext configuration, String userContainerName){
    return userContainerName.endsWith(Joiner.on(CONTAINER_SEPARATOR).join(configuration.getNamespace(), SYSTEM_NAMESPACE))
      ? userContainerName
      : Joiner.on(CONTAINER_SEPARATOR).join(userContainerName, configuration.getNamespace(), SYSTEM_NAMESPACE);
  }

  public static String getConfigLabelOfLayerProvider(Class<? extends LayerProvider> clazz){
    return Joiner.on('.').join(clazz.getName(), "config");
  }

  public static String getConfigLabelOfLayerNode(Class<? extends LayerProvider> clazz){
    return Joiner.on('.').join(clazz.getName(), "node","config");
  }

  public static Map<String, String> labelizeLayerNode(Class<? extends LayerProvider> providerClass, int layerLevel,
                                                      DockerContext dockerContext, String random)
  {
    return ImmutableMap.of(
      LAYER_INDEX, Integer.toString(layerLevel),
      LAYER_PROVIDER_LABEL, providerClass.getName(),
      SYSTEM_LABEL, "",
      NAMESPACE_LABEL, dockerContext.getNamespace(),
      RANDOM_LABEL, random
    );
  }

  public static Map<String, String> filterLayerNode(Class<? extends LayerProvider> providerClass, DockerContext dockerContext)
  {
    return ImmutableMap.of(
      LAYER_PROVIDER_LABEL, providerClass.getName(),
      SYSTEM_LABEL, "",
      NAMESPACE_LABEL, dockerContext.getNamespace()
    );
  }

  public static Map<String, String> filterLayerNode(Class<? extends LayerProvider> providerClass, int layerLevel, DockerContext dockerContext)
  {
    return ImmutableMap.of(
      LAYER_INDEX, Integer.toString(layerLevel),
      LAYER_PROVIDER_LABEL, providerClass.getName(),
      SYSTEM_LABEL, "",
      NAMESPACE_LABEL, dockerContext.getNamespace()
    );
  }

  public static Map<String, String> labelizeNamedEntity(String name, DockerContext dockerContext){
    return ImmutableMap.of(
      ORIGINAL_LABEL, name,
      SYSTEM_LABEL, "",
      NAMESPACE_LABEL, dockerContext.getNamespace()
    );
  }

  public static Map<String, String> labelizeLayerEntity(int layerLevel){
    return ImmutableMap.of(
      LAYER_INDEX, Integer.toString(layerLevel)
    );
  }

  public static Map<String, String> filterNamedEntity(DockerContext dockerContext){
    return ImmutableMap.of(
      SYSTEM_LABEL, "",
      NAMESPACE_LABEL, dockerContext.getNamespace()
    );
  }

  public static String soxyDriverName(DockerContext dockerContext){
    return Joiner.on("__").join(dockerContext.getNamespace(), SOXY_DRIVER_NAME);
  }



}

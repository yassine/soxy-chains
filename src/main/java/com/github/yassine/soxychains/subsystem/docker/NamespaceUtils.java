package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.common.base.Joiner;

public class NamespaceUtils {

  private static final String SYSTEM_NAMESPACE    = "soxy_chains";
  private static final String IMAGE_SEPARATOR     = "__";
  private static final String CONTAINER_SEPARATOR = "__";
  private static final String NETWORK_SEPARATOR   = "__";
  public static final String SYSTEM_LABEL         = SoxyChainsModule.class.getPackage().getName();
  public static final String ORIGINAL_LABEL       = SYSTEM_LABEL+".original_name";
  public static final String NAMESPACE_LABEL      = SYSTEM_LABEL+".namespace";
  public static final String REPLICA_LABEL        = SYSTEM_LABEL+".replica_num";

  public static String nameSpaceImage(DockerConfiguration configuration, String userImageName){
    return userImageName.startsWith(Joiner.on(IMAGE_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userImageName
      : Joiner.on(IMAGE_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userImageName);
  }

  public static String nameSpaceNetwork(DockerConfiguration configuration, String userNetworkName){
    return userNetworkName.startsWith(Joiner.on(NETWORK_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userNetworkName
      : Joiner.on(NETWORK_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userNetworkName);
  }

  public static String nameSpaceContainer(DockerConfiguration configuration, String userContainerName){
    return userContainerName.startsWith(Joiner.on(CONTAINER_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userContainerName
      : Joiner.on(CONTAINER_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userContainerName);
  }

}

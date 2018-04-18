package com.github.yassine.soxychains.subsystem.layer.spi.ovpn;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.Device;
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@AutoService(LayerProvider.class)
public class OpenVPNLayerProvider implements LayerProvider<OpenVPNLayerConfiguration, OpenVPNNodeConfiguration> {

  private static final DockerImage IMAGE = new DockerImage("openvpn-node", URI.create("classpath://com/github/yassine/soxychains/subsystem/layer/spi/ovpn"), ImmutableMap.of());

  @Override
  public void configureNode(CreateContainerCmd containerCmd, OpenVPNNodeConfiguration nodeConfiguration, OpenVPNLayerConfiguration layerConfiguration) {
    containerCmd.withEnv(
      Stream.of(
        of("OVPN_CONFIG="+nodeConfiguration.getConfiguration().getBase64Configuration()),
        ofNullable(nodeConfiguration.getConfiguration().getUser())
          .map(user -> "OVPN_USER="+user),
        ofNullable(nodeConfiguration.getConfiguration().getPassword())
          .map(password -> "OVPN_PASS="+password)
      ).filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList())
    ).withCapAdd(Capability.NET_ADMIN).withDevices(Device.parse("/dev/net/tun:/dev/net/tun"));
  }

  @Override
  public DockerImage image(OpenVPNLayerConfiguration layerConfiguration) {
    return IMAGE;
  }
}

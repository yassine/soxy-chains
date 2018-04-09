package com.github.yassine.soxychains.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.yassine.soxychains.SoxyChainsApplication
import com.github.yassine.soxychains.SoxyChainsConfiguration
import com.github.yassine.soxychains.TestUtils
import com.google.common.io.Files
import org.apache.commons.io.IOUtils
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceImage
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceNetwork
import static java.util.Arrays.stream

@Stepwise
class InstallCommandSpec extends Specification {

  @Shared
  private ObjectMapper mapper = TestUtils.mapper()
  @Shared
  private SoxyChainsConfiguration configuration

  def "it should create docker images for required services"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("install", "-c", config.getAbsolutePath())
    def configuration = mapper.readValue(config, SoxyChainsConfiguration.class)
    def dockerClient = TestUtils.dockerClient(configuration.getDocker().getHosts().get(0))

    expect:
    dockerClient.listImagesCmd().exec().stream().anyMatch{ image ->
      stream(image.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "consul"))}
    }
    dockerClient.listImagesCmd().exec().stream().anyMatch{ image ->
      stream(image.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "gobetween"))}
    }
    dockerClient.listImagesCmd().exec().stream().anyMatch{ image ->
      stream(image.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "dns_server"))}
    }
    dockerClient.listNetworksCmd().exec().stream().anyMatch{ network ->
      (nameSpaceNetwork(configuration.getDocker(), configuration.getDocker().getNetworkingConfiguration().getNetworkName()) == network.getName())
    }
  }

  def "it should remove docker images for required services"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("uninstall", "-c", config.getAbsolutePath())
    def configuration = mapper.readValue(config, SoxyChainsConfiguration.class)
    def dockerClient = TestUtils.dockerClient(configuration.getDocker().getHosts().get(0))

    expect:
    dockerClient.listImagesCmd().exec().stream().allMatch{ image ->
      stream(image.getRepoTags()).noneMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "consul"))}
    }
    dockerClient.listImagesCmd().exec().stream().allMatch{ image ->
      stream(image.getRepoTags()).noneMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "gobetween"))}
    }
    dockerClient.listImagesCmd().exec().stream().allMatch{ image ->
      stream(image.getRepoTags()).noneMatch{tag -> tag.contains(nameSpaceImage(configuration.getDocker(), "dns_server"))}
    }
    dockerClient.listNetworksCmd().exec().stream().noneMatch{ network ->
      (nameSpaceNetwork(configuration.getDocker(), configuration.getDocker().getNetworkingConfiguration().getNetworkName()) == network.getName())
    }
  }



}

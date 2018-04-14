package com.github.yassine.soxychains.cli

import com.github.dockerjava.api.model.Container
import com.github.yassine.soxychains.SoxyChainsApplication
import com.github.yassine.soxychains.SoxyChainsConfiguration
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.TestUtils
import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration
import com.google.common.collect.ImmutableMap
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Injector
import io.reactivex.Observable
import org.apache.commons.io.IOUtils
import spock.guice.UseModules
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.stream.Collectors

import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.SOXY_DRIVER_NAME
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceContainer
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceImage
import static java.util.Arrays.stream

@Stepwise @UseModules(TestModule)
class StartCommandSpec extends Specification {

  @Inject
  private SoxyChainsConfiguration soxyChainsConfiguration;
  @Inject
  private Set<ImageRequirer> imageRequirers
  @Inject
  private DockerConfiguration configuration
  @Inject
  private Set<ServicesPlugin> services
  @Inject
  private Injector injector

  def "it should create services docker images if missing and start required services"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("up", "-c", config.getAbsolutePath())
    def dockerClient     = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages     = dockerClient.listImagesCmd().exec()
    def dockerContainers = dockerClient.listContainersCmd().withLabelFilter(ImmutableMap.of(NamespaceUtils.SYSTEM_LABEL, "",NamespaceUtils.NAMESPACE_LABEL, configuration.getNamespace()))
      .exec().stream().filter{container -> container.getStatus().contains("Up")}.collect(Collectors.<Container>toList())

    expect:
    //all the required images have been created
    Observable.fromIterable(imageRequirers)
      .flatMap({ requirer -> requirer.require() })
      .map({ requiredImage -> requiredImage.getName() })
      .map({ requiredImage -> dockerImages.stream().anyMatch{ repoImage ->
        stream(repoImage.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration, requiredImage))}
      }})
      .reduce(true, { a, b -> a && b })
      .blockingGet()

    //the driver is running under the config provided namespace
    dockerClient.listContainersCmd()
      .withLabelFilter(NamespaceUtils.labelizeNamedEntity(SOXY_DRIVER_NAME, configuration))
      .exec().stream()
      .filter{container -> stream(container.getNames()).anyMatch{name -> name.contains(nameSpaceContainer(configuration, SOXY_DRIVER_NAME))}}
      .filter{container -> container.getStatus().contains("Up")}
      .findAny().isPresent()

    //main network have been created using the soxy-driver
    dockerClient.listNetworksCmd().exec()
      .stream()
      .filter{ network -> network.getDriver() == NamespaceUtils.soxyDriverName(configuration) }
      .filter{ network -> network.getName().contains(NamespaceUtils.nameSpaceNetwork(configuration, configuration.getNetworkingConfiguration().getNetworkName())) }
      .findAny()
      .isPresent()

    //all services are up & running
    services.stream()
      .map{ service -> (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) service.getClass())) }
      .map{ serviceConfig -> nameSpaceContainer(configuration, serviceConfig.serviceName()) }
      .allMatch{ serviceName -> dockerContainers.stream()
        .filter{ container ->
          stream(container.getNames()).anyMatch{ name -> (name.contains(nameSpaceContainer(configuration, serviceName))) }
        }
        .findAny().isPresent()
      }
  }

  def "it should remove all the services containers but keep their images"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("down", "-c", config.getAbsolutePath())
    def dockerClient     = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages     = dockerClient.listImagesCmd().exec()
    def dockerContainers = dockerClient.listContainersCmd().withLabelFilter(ImmutableMap.of(NamespaceUtils.SYSTEM_LABEL, "",NamespaceUtils.NAMESPACE_LABEL, configuration.getNamespace()))
      .exec().stream().filter{container -> container.getStatus().contains("Up")}.collect(Collectors.<Container>toList())

    expect:
    //all the required images still exist
    Observable.fromIterable(imageRequirers)
      .flatMap({ requirer -> requirer.require() })
      .map({ requiredImage -> requiredImage.getName() })
      .map({ requiredImage -> dockerImages.stream().anyMatch{ repoImage ->
        stream(repoImage.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration, requiredImage))}
      }})
      .reduce(true, { a, b -> a && b })
      .blockingGet()
    //all services are removed
    services.stream()
      .map{ service -> (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) service.getClass())) }
      .map{ serviceConfig -> nameSpaceContainer(configuration, serviceConfig.serviceName()) }
      .allMatch{ serviceName -> dockerContainers.stream().allMatch{ container ->
        stream(container.getNames()).allMatch{name -> !(name.contains(nameSpaceContainer(configuration, serviceName))) }
      }}
  }

  static class TestModule extends AbstractModule{
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("config-install.yaml")
      install(new SoxyChainsModule(is))
    }
  }

}

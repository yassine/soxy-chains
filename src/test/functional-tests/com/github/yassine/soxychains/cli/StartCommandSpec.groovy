package com.github.yassine.soxychains.cli

import com.ecwid.consul.v1.QueryParams
import com.github.dockerjava.api.model.Container
import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsContext
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.TestUtils
import com.github.yassine.soxychains.core.Phase
import com.github.yassine.soxychains.core.PhaseRunner
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkHelper
import com.github.yassine.soxychains.subsystem.layer.AbstractLayerConfiguration
import com.github.yassine.soxychains.subsystem.layer.LayerNode
import com.github.yassine.soxychains.subsystem.layer.LayerProvider
import com.github.yassine.soxychains.subsystem.layer.LayerService
import com.github.yassine.soxychains.subsystem.layer.spi.ovpn.OpenVPNConfiguration
import com.github.yassine.soxychains.subsystem.layer.spi.ovpn.OpenVPNLayerConfiguration
import com.github.yassine.soxychains.subsystem.layer.spi.ovpn.OpenVPNNodeConfiguration
import com.github.yassine.soxychains.subsystem.layer.spi.tor.TorLayerConfiguration
import com.github.yassine.soxychains.subsystem.layer.spi.tor.TorNodeConfiguration
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration
import com.github.yassine.soxychains.subsystem.service.consul.ConsulProvider
import com.github.yassine.soxychains.subsystem.service.consul.ServiceScope
import com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration
import com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenProvider
import com.github.yassine.soxychains.web.WebAPIModule
import com.google.common.collect.ImmutableMap
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Injector
import groovy.util.logging.Slf4j
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.IOUtils
import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.stream.Collector
import java.util.stream.Collectors

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING
import static com.github.yassine.soxychains.core.FluentUtils.getWithRetry
import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*
import static com.github.yassine.soxychains.subsystem.docker.networking.NetworkHelper.SOXY_DRIVER_PROXY_HOST_OPTION
import static com.github.yassine.soxychains.subsystem.docker.networking.NetworkHelper.SOXY_DRIVER_PROXY_PORT_OPTION
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.namespaceLayerService
import static com.github.yassine.soxychains.subsystem.service.consul.ConsulUtils.portShift
import static com.github.yassine.soxychains.subsystem.service.consul.ServiceScope.CLUSTER
import static com.github.yassine.soxychains.subsystem.service.consul.ServiceScope.LOCAL
import static io.reactivex.Observable.fromCallable
import static io.reactivex.Observable.fromIterable
import static java.lang.String.format
import static java.util.Arrays.asList
import static java.util.Arrays.stream
import static java.util.stream.IntStream.range

@UseModules(TestModule) @Stepwise @Slf4j
class StartCommandSpec extends Specification {

  @Inject
  private SoxyChainsContext soxyChainsConfiguration
  @Inject
  private Set<ImageRequirer> imageRequirers
  @Inject
  private DockerContext configuration
  @Inject
  private Set<ServicesPlugin> services
  @Inject
  private Injector injector
  @Inject
  private LayerService layerService
  @Inject
  private Map<Class<? extends AbstractLayerConfiguration>, LayerProvider> providers
  @Inject
  private ConsulProvider consulProvider
  @Inject
  private GobetweenProvider gobetweenProvider
  @Inject @Shared
  private PhaseRunner phaseRunner
  @Inject
  private NetworkHelper networkHelper
  @Inject
  private GobetweenConfiguration gobetweenConfiguration
  @Inject
  private DockerProvider dockerProvider

  def "it should create services docker images if missing and start required services"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    Application.main("up", "-c", config.getAbsolutePath())
    def dockerClient     = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages     = dockerClient.listImagesCmd().exec()
    def dockerContainers = dockerClient.listContainersCmd().withLabelFilter(ImmutableMap.of(SYSTEM_LABEL, "",NAMESPACE_LABEL, configuration.getNamespace()))
      .exec().stream().filter{container -> container.getStatus().contains("Up")}.collect(Collectors.<Container>toList())

    expect:
    //all the required images have been created
    fromIterable(imageRequirers)
      .flatMap({ requirer -> requirer.require() })
      .map{ DockerImage requiredImage -> requiredImage.getName() }
      .map{ String requiredImage -> dockerImages.stream().anyMatch{ repoImage ->
        stream(repoImage.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration, requiredImage))}
      }}
      .reduce(true, { a, b -> a && b })
      .blockingGet()

    //the driver is running under the config provided namespace
    dockerClient.listContainersCmd()
      .withLabelFilter(labelizeNamedEntity(SOXY_DRIVER_NAME, configuration))
      .exec().stream()
      .filter{container -> stream(container.getNames()).anyMatch{name -> name.contains(nameSpaceContainer(configuration, SOXY_DRIVER_NAME))}}
      .filter{container -> container.getStatus().contains("Up")}
      .findAny().isPresent()

    //main network have been created using the soxy-driver
    dockerClient.listNetworksCmd().exec()
      .stream()
      .filter{ network -> network.getDriver() == soxyDriverName(configuration) }
      .filter{ network -> network.getName().contains(nameSpaceNetwork(configuration, configuration.getNetworkingConfiguration().getNetworkName())) }
      .findAny()
      .isPresent()

    //all services are up & running
    services.stream()
      .map{ service -> injector.getInstance(configClassOf((Class) service.getClass())) }
      .map{ ServicesPluginConfiguration serviceConfig -> nameSpaceContainer(configuration, serviceConfig.serviceName()) }
      .allMatch{ String serviceName -> dockerContainers.stream()
        .filter{ container ->
          stream(container.getNames()).anyMatch{ name -> (name.contains(nameSpaceContainer(configuration, serviceName))) }
        }
        .findAny().isPresent()
      }
  }

  def "it would be able to add nodes through the api" () {
    setup:
    def torLayer  = soxyChainsConfiguration.getLayers().get(0)
    def ovpnLayer = soxyChainsConfiguration.getLayers().get(1)
    def torLayerNode  = new LayerNode(0, torLayer, new TorNodeConfiguration())
    List<LayerNode> ovpnLayerNodes = TestUtils.findOnlineVPNConfigurations(3).stream()
      .map{ovpnConfig -> new LayerNode(1, ovpnLayer, new OpenVPNNodeConfiguration().setConfiguration(new OpenVPNConfiguration()
      .setBase64Configuration(ovpnConfig)
      .setUser("vpngate")
      .setPassword("vpngate"))) }.collect(Collectors.toList() as Collector<? super Object, Object, Object>)

    def dockerClient  = TestUtils.dockerClient(configuration.getHosts().get(0))
    def torLayerProvider = providers.get(TorLayerConfiguration.class)
    def vpnLayerProvider = providers.get(OpenVPNLayerConfiguration.class)
    Class<? extends LayerProvider> torLayerProviderClass = (Class<? extends LayerProvider>) torLayerProvider.getClass()
    Class<? extends LayerProvider> vpnLayerProviderClass = (Class<? extends LayerProvider>) vpnLayerProvider.getClass()
    def torFilters       = filterLayerNode(torLayerProviderClass, 0, configuration)
    def ovpnFilters      = filterLayerNode(vpnLayerProviderClass, 1, configuration)

    when:
    layerService.add(torLayerNode).blockingGet()
    fromIterable(ovpnLayerNodes)
      .flatMapSingle{LayerNode layerNode -> layerService.add(layerNode).subscribeOn(Schedulers.io())}
      .blockingSubscribe()

    then:
    // A tor node is present @ layer 0
    dockerClient.listContainersCmd()
      .withLabelFilter(torFilters)
      .exec().stream().findAny()
      .isPresent()
    // A vpn node is present @ layer 1
    dockerClient.listContainersCmd()
      .withLabelFilter(ovpnFilters)
      .exec().stream().findAny()
      .isPresent()
  }

  def "it would be able to remove nodes through the api" () {
    setup:
    def torLayer  = soxyChainsConfiguration.getLayers().get(0)
    def layerNode     = new LayerNode(0, torLayer, new TorNodeConfiguration())
    def dockerClient  = TestUtils.dockerClient(configuration.getHosts().get(0))
    def layerProvider = providers.get(TorLayerConfiguration.class)
    Class<? extends LayerProvider> providerClass = (Class<? extends LayerProvider>) layerProvider.getClass()
    def filters       = filterLayerNode(providerClass, 0, configuration)
    //adding some nodes
    layerService.add(layerNode).blockingGet()
    layerService.add(layerNode).blockingGet()

    def size = dockerClient.listContainersCmd()
                .withLabelFilter(filters)
                .exec().size()

    when:
    layerService.remove(layerNode).blockingGet()

    then:
    //the number of nodes should decrease by one
    dockerClient.listContainersCmd()
      .withLabelFilter(filters)
      .exec().size() == size - 1

  }

  def "nodes are automatically published in consul" () {
    setup:
    def consul = consulProvider.get(configuration.getHosts().get(0))

    expect:
    //local & cluster services exists
    consul.getCatalogServices(QueryParams.DEFAULT).getValue()
      .keySet().containsAll(
        range(0, soxyChainsConfiguration.getLayers().size()).boxed()
          .flatMap{ index -> asList(namespaceLayerService(index, LOCAL), namespaceLayerService(index, CLUSTER)).stream() }
          .collect(Collectors.toList())
      )
  }

  def "gobetween services backends are configured" () {
    setup:
    def gobetween = gobetweenProvider.getClient(configuration.getHosts().get(0))
    expect:
    gobetween.getServers().keySet()
      .containsAll(
        range(0, soxyChainsConfiguration.getLayers().size()).boxed()
          .flatMap{index -> stream(ServiceScope.values()).map{serviceScope -> namespaceLayerService(index, serviceScope)} }
          .collect(Collectors.toList() as Collector<? super Object, Object, String>)
      )
  }

  def "layers networks should be correctly configured" () {
    setup:
    def dockerClient  = TestUtils.dockerClient(configuration.getHosts().get(0))
    def docker        = dockerProvider.get(configuration.getHosts().get(0))
    expect:
    fromIterable(range(1, soxyChainsConfiguration.getLayers().size()).boxed().collect(Collectors.<Integer>toList()))
      .flatMap{layerIndex -> fromCallable({ ->
        def networks = dockerClient.listNetworksCmd().exec()
        def currentNetwork = networks.stream().filter{net -> net.getName().contains(nameSpaceLayerNetwork(configuration, layerIndex))}.findAny().get()
        def parentConfiguration = soxyChainsConfiguration.getLayers().get(layerIndex - 1)
        def gobetweenAddress = networkHelper.getGobetweenAddress(docker).blockingGet()

        println format("Expecting %s, found %s", Integer.toString(portShift(layerIndex-1, parentConfiguration.getClusterServicePort())), currentNetwork.getOptions().get(SOXY_DRIVER_PROXY_PORT_OPTION))
        println format("Expecting %s, found %s", currentNetwork.getOptions().get(SOXY_DRIVER_PROXY_HOST_OPTION), gobetweenAddress)
        return currentNetwork.getOptions().get(SOXY_DRIVER_PROXY_PORT_OPTION) == Integer.toString(portShift(layerIndex-1, parentConfiguration.getClusterServicePort())) && \
               currentNetwork.getOptions().get(SOXY_DRIVER_PROXY_HOST_OPTION) == gobetweenAddress
      })}
      .reduce(true, { a,b -> a && b})
      .blockingGet()
  }

  def "services status should be 'passing' after a reasonable amount of time" () {
    setup:
    def consul = consulProvider.get(configuration.getHosts().get(0))
    List<String> services = asList(namespaceLayerService(0, LOCAL), namespaceLayerService(0, CLUSTER), namespaceLayerService(1, LOCAL), namespaceLayerService(1, CLUSTER))
    expect:
    fromIterable(services)
      .flatMapMaybe{ service ->
        return getWithRetry({ ->
          def result = consul.getHealthChecksForService(service, QueryParams.DEFAULT).getValue()
            .stream().map{check -> (check.getStatus() == PASSING)}
            .reduce(false, {a,b -> a || b})
          if(!result){
            throw new RuntimeException(format("Failed to get service '%s' as healthy.", service))
          }
          consul.getHealthChecksForService(service, QueryParams.DEFAULT).getValue().forEach{
            check ->
            log.info("service '{}' with name '{}' has status '{}'.", check.getServiceId(), check.getServiceName(), check.getStatus())
          }
          return result
        },{ retries -> log.info("successfully got service '{}' up after {} retries.", service, retries) },
          { retries -> log.error("failed to get service '{}' up after {} retries.", service, retries) }, 180, 1000)
          .onErrorResumeNext(Maybe.just(false)).defaultIfEmpty(false)
      }
      .reduce(true, { a,b -> a && b})
      .blockingGet()
  }

  def "it should remove all the services containers but keep the images"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    Application.main("down", "-c", config.getAbsolutePath())
    def dockerClient     = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages     = dockerClient.listImagesCmd().exec()
    def dockerContainers = dockerClient.listContainersCmd().withLabelFilter(ImmutableMap.of(SYSTEM_LABEL, "",NAMESPACE_LABEL, configuration.getNamespace()))
      .exec().stream().filter{container -> container.getStatus().contains("Up")}.collect(Collectors.<Container>toList())
    def torLayer  = soxyChainsConfiguration.getLayers().get(0)
    def layerNode     = new LayerNode(0, torLayer, new TorNodeConfiguration())
    def layerProvider = providers.get(TorLayerConfiguration.class)
    Class<? extends LayerProvider> providerClass = (Class<? extends LayerProvider>) layerProvider.getClass()
    def filters       = filterLayerNode(providerClass, 0, configuration)
    layerService.add(layerNode)

    expect:
    //all the required images still exist
    fromIterable(imageRequirers)
      .flatMap{ requirer -> requirer.require() }
      .map{ DockerImage requiredImage -> requiredImage.getName() }
      .map{ String requiredImage -> dockerImages.stream().anyMatch{ repoImage ->
        stream(repoImage.getRepoTags()).anyMatch{tag -> tag.contains(nameSpaceImage(configuration, requiredImage))}
      }}
      .reduce(true, { a, b -> a && b })
      .blockingGet()
    //all services are removed
    services.stream()
      .map{ service -> injector.getInstance(configClassOf((Class) service.getClass())) }
      .map{ ServicesPluginConfiguration serviceConfig -> nameSpaceContainer(configuration, serviceConfig.serviceName()) }
      .allMatch{ String serviceName -> dockerContainers.stream().allMatch{ container ->
        stream(container.getNames()).allMatch{name -> !(name.contains(nameSpaceContainer(configuration, serviceName))) }
      }}
    //all nodes have been removed
    !dockerClient.listContainersCmd()
      .withLabelFilter(filters)
      .exec().stream().findAny()
      .isPresent()
  }

  def cleanupSpec (){
    phaseRunner.runPhase(Phase.UNINSTALL).blockingGet()
  }

  static class TestModule extends AbstractModule{
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("config-install.yaml")
      install(new ConfigurationModule(is))
      install(new SoxyChainsModule())
      install(new WebAPIModule())
    }
  }

}

package com.github.yassine.soxychains.subsystem.docker.client

import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsContext
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(TestModule)
class HostManagerSupportSpec extends Specification {
  @Inject
  private HostManager hostManager
  @Inject
  private SoxyChainsContext soxyChainsContext

  def "List: it should return the online hosts only"() {
    setup:
    List<DockerHostConfiguration> hosts = hostManager.list().toList().blockingGet()
    expect:
    hosts.size() == 1
    soxyChainsContext.getDocker().getHosts().size() == 2
    hosts.stream().map{host -> host.getUri().toString().startsWith("unix")}.findAny().isPresent()

  }

  static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("host-manager-test.yaml")
      install(new ConfigurationModule(is))
      install(new SoxyChainsModule())
    }
  }
}

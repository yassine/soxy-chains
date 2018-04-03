package com.github.yassine.soxychains.subsystem.service

import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.service.consul.ConsulServicesPluginConfiguration
import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Specification

class ServicesModuleSpec extends Specification {
  def "it should create a binding for services configurations"() {
    setup:
    InputStream config = getClass().getResourceAsStream("service-module-config.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    ConsulServicesPluginConfiguration configuration = injector.getInstance(ConsulServicesPluginConfiguration.class)
    then:
    configuration != null
  }
  def "it should create a binding for services configurations when services configuration is overriden by the user"() {
    setup:
    InputStream config = getClass().getResourceAsStream("service-module-config-with-services.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    ConsulServicesPluginConfiguration configuration = injector.getInstance(ConsulServicesPluginConfiguration.class)
    then:
    configuration != null
    configuration.imageName() == 'consul-test'
  }
}

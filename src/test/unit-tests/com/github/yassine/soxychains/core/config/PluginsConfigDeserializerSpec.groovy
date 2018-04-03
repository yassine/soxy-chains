package com.github.yassine.soxychains.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(PluginTestFixtures.TestModule)
class PluginsConfigDeserializerSpec extends Specification {

  @Inject
  ObjectMapper objectMapper

  def "Deserialize"() {
    when:
    InputStream is = getClass().getResourceAsStream("deserialize_config.yaml")
    PluginTestFixtures.RootConfiguration rootConfiguration = objectMapper.readValue(is, PluginTestFixtures.RootConfiguration.class)
    PluginTestFixtures.PluginAConfiguration configA = (PluginTestFixtures.PluginAConfiguration) rootConfiguration.getPlugins().get("plugin_a")
    PluginTestFixtures.PluginBConfiguration configB = (PluginTestFixtures.PluginBConfiguration) rootConfiguration.getPlugins().get("plugin_b")
    then:
    rootConfiguration.getPlugins().get("plugin_a") != null
    configA.getParamA() == "valueA"
    configB.getParamB() == "valueB"
  }

}

package com.github.yassine.soxychains.subsystem.layer

import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(LayerModule)
class LayerModuleSpec extends Specification {
  @Inject
  Map<Class<? extends AbstractLayerConfiguration>, LayerService> layerIndex;
  def "Configure"() {

    expect:
      layerIndex != null
  }
}

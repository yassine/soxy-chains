package com.github.yassine.soxychains.subsystem.docker.networking

import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import spock.lang.Specification

class NetworkingImageRequirerSpec extends Specification {

  def "get: it should return the soxy driver image" () {
    given:
    def requirer = new NetworkingImageRequirer(new DockerConfiguration())
    def image = requirer.require().blockingFirst()
    expect:
    image.getName() == NamespaceUtils.SOXY_DRIVER_NAME
    image.getRoot().toString().endsWith(NamespaceUtils.SOXY_DRIVER_NAME.replaceAll("-","_"))
  }
}

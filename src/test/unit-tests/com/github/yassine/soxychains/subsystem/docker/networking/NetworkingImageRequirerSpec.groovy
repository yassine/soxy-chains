package com.github.yassine.soxychains.subsystem.docker.networking

import spock.lang.Specification

class NetworkingImageRequirerSpec extends Specification {

  def "get: it should return the soxy driver image" () {
    given:
    def requirer = new NetworkingImageRequirer()
    def image = requirer.require().blockingFirst()
    expect:
    image.getName() == NetworkingImageRequirer.IMAGE_NAME
    image.getRoot().toString().endsWith(NetworkingImageRequirer.IMAGE_NAME.replaceAll("-","_"))
  }
}

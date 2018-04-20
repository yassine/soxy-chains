package com.github.yassine.soxychains.subsystem.docker.config

import spock.lang.Specification

class DockerHostConfigurationSpec extends Specification {

  def "it should return the loopback interface address if the host is based on unix sockets"() {
    given:
    DockerHostConfiguration dockerHostConfiguration = new DockerHostConfiguration()
    dockerHostConfiguration.setUri(URI.create("unix:///run/docker.sock"))
    expect:
    dockerHostConfiguration.getHostname() == '127.0.1.1'
  }

  def "it should return the uri host address if the host is based on remote uri"() {
    given:
    DockerHostConfiguration dockerHostConfiguration = new DockerHostConfiguration()
    dockerHostConfiguration.setUri(URI.create("tcp://github.com/yassine"))
    expect:
    dockerHostConfiguration.getHostname() == 'github.com'
  }

}

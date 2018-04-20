package com.github.yassine.soxychains.subsystem.docker.networking.task

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.command.CreateNetworkCmd
import com.github.dockerjava.api.model.Container
import com.github.yassine.soxychains.SoxyChainsConfiguration
import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration
import io.reactivex.Maybe
import spock.lang.Specification

import java.util.function.Consumer

class NetworkingStartupTaskSpec extends Specification {

  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  SoxyChainsConfiguration soxyChainsConfiguration = new SoxyChainsConfiguration()
  DockerConfiguration configuration = soxyChainsConfiguration.getDocker()
  NetworkingConfiguration networkingConfiguration = configuration.getNetworkingConfiguration()
  NetworkingStartupTask task = new NetworkingStartupTask(dockerProvider, configuration, networkingConfiguration)
  Container container = Mock()
  void setup () {
    dockerProvider.dockers() >> [ docker ]
    container.getId() >> "my-id"
  }

  def "execute: it should return true when networks are successfully created" () {
    setup:
    docker.runContainer(_ as String, _ as String, _ as Consumer<CreateContainerCmd>) >> Maybe.just(container)
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>) >> Maybe.just("network_id")
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>, _ as Consumer<String>) >> Maybe.just("network_id")
    docker.joinNetwork(_ as Container, _ as String) >> Maybe.just(true)
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return false when networks fail to get created" () {
    setup:
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>) >> Maybe.empty()
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>, _ as Consumer<String>) >> Maybe.empty()
    docker.runContainer(_ as String, _ as String, _ as Consumer<CreateContainerCmd>) >> Maybe.just(container)
    docker.joinNetwork(_ as Container, _ as String) >> Maybe.just(false)
    expect:
    !task.execute().blockingGet()
  }


}

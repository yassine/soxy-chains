package com.github.yassine.soxychains.subsystem.docker.networking.task

import com.github.dockerjava.api.command.RemoveContainerCmd
import com.github.dockerjava.api.command.RemoveNetworkCmd
import com.github.dockerjava.api.command.StopContainerCmd
import com.github.yassine.soxychains.SoxyChainsContext
import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration
import io.reactivex.Maybe
import io.reactivex.Observable
import spock.lang.Specification

import java.util.function.Consumer

class NetworkingStopTaskSpec extends Specification {

  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  SoxyChainsContext soxyChainsConfiguration = new SoxyChainsContext()
  DockerContext configuration = soxyChainsConfiguration.getDocker()
  NetworkingConfiguration networkingConfiguration = configuration.getNetworkingConfiguration()
  NetworkingStopTask task = new NetworkingStopTask(dockerProvider, configuration, networkingConfiguration)

  void setup () {
    dockerProvider.dockers() >> Observable.fromIterable([docker])
    docker.stopContainer(_ as String, _ as Consumer<StopContainerCmd>, _ as Consumer<String>, _ as Consumer<RemoveContainerCmd>, _ as Consumer<String>) >> Maybe.just(true)
    docker.stopContainer(_ as String) >> Maybe.just(true)
  }

  def "execute: it should return true when networks are successfully created" () {
    setup:
    docker.removeNetwork(_ as String) >> Maybe.just(true)
    docker.removeNetwork(_ as String, _ as Consumer<RemoveNetworkCmd>, _ as Consumer<String>) >> Maybe.just(true)
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return true when networks fail to get created" () {
    setup:
    docker.removeNetwork(_ as String) >> Maybe.just(false)
    docker.removeNetwork(_ as String, _ as Consumer<RemoveNetworkCmd>, _ as Consumer<String>) >> Maybe.just(false)
    expect:
    !task.execute().blockingGet()
  }

}

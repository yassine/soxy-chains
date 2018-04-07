package com.github.yassine.soxychains.subsystem.docker.networking.task

import com.github.dockerjava.api.command.CreateNetworkCmd
import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration
import io.reactivex.Maybe
import spock.lang.Specification

import java.util.function.Consumer

class NetworkingInstallTaskSpec extends Specification {

  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  DockerConfiguration configuration = new DockerConfiguration()
  NetworkingConfiguration networkingConfiguration = new NetworkingConfiguration()
  NetworkingInstallTask task = new NetworkingInstallTask(dockerProvider, configuration, networkingConfiguration)

  void setup () {
    dockerProvider.dockers() >> [ docker ]
  }

  def "execute: it should return true when networks are successfully created" () {
    setup:
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>, _ as Consumer<String>) >> Maybe.just("network_id")
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return true when networks fail to get created" () {
    setup:
    docker.createNetwork(_ as String, _ as Consumer<CreateNetworkCmd>, _ as Consumer<String>) >> Maybe.empty()
    expect:
    !task.execute().blockingGet()
  }


}

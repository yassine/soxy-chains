package com.github.yassine.soxychains.subsystem.docker.image.task

import com.github.dockerjava.api.command.BuildImageCmd
import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer
import com.github.yassine.soxychains.subsystem.docker.image.resolver.DockerImageResolver
import io.reactivex.Maybe
import io.reactivex.Observable
import spock.lang.Specification

import java.util.function.Consumer

class ImageInstallTaskSpec extends Specification {

  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  DockerConfiguration configuration = new DockerConfiguration()
  ImageRequirer imageRequirer = Mock()
  DockerImageResolver dockerImageResolver = Mock()
  ImageInstallTask task = new ImageInstallTask([imageRequirer] as Set, dockerImageResolver, dockerProvider, configuration)

  void setup () {
    dockerProvider.dockers() >> [docker]
    imageRequirer.require()  >> Observable.just(new DockerImage("test-image", null, null))
  }

  def "execute: it should return true if all the necessary image have installed"() {
    setup:
    docker.buildImage(_ as String, _ as Consumer<BuildImageCmd>) >> Maybe.just("test")
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return true if some necessary image have failed"() {
    setup:
    docker.buildImage(_ as String, _ as Consumer<BuildImageCmd>) >> Maybe.empty()
    expect:
    !task.execute().blockingGet()
  }

}

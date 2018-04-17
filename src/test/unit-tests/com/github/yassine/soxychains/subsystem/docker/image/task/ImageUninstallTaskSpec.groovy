package com.github.yassine.soxychains.subsystem.docker.image.task

import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer
import io.reactivex.Maybe
import io.reactivex.Observable
import spock.lang.Specification

class ImageUninstallTaskSpec extends Specification {
  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  DockerConfiguration configuration = new DockerConfiguration()
  ImageRequirer imageRequirer = Mock()
  ImageUninstallTask task = new ImageUninstallTask([imageRequirer] as Set, dockerProvider, configuration)

  void setup () {
    dockerProvider.dockers() >> [docker]
    imageRequirer.require()  >> Observable.just(new DockerImage("test-image", null, null))
  }

  def "execute: it should return true if all the necessary image have uninstalled"() {
    setup:
    docker.removeImage(_ as String) >> Maybe.just(true)
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return true if some necessary image uninstall have failed"() {
    setup:
    docker.removeImage(_ as String) >> Maybe.just(false)
    expect:
    !task.execute().blockingGet()
  }
}

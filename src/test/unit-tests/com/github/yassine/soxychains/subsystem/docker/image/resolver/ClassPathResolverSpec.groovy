package com.github.yassine.soxychains.subsystem.docker.image.resolver

import com.google.common.collect.ImmutableMap
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import spock.lang.Shared
import spock.lang.Specification

class ClassPathResolverSpec extends Specification {

  @Shared
  DockerImageResourceResolver resolver

  def setupSpec() {
    Injector injector = Guice.createInjector();
    resolver = injector.getInstance(ClassPathResolver.class)
  }

  def "It should create a tar archive of the docker image resources used to build the Docker image from the class path" () {
    setup:
    def expectedMap = [Dockerfile: 'nginx', 'config/app.config':'${no-change}', 'config/level2/level2':'nginx']
    def testResult
    when:
    InputStream is = resolver.resolve("com/github/yassine/soxychains/subsystem/docker/image/resolver/test_image", ImmutableMap.of("image","nginx"))
    TarArchiveInputStream tais = new TarArchiveInputStream(is)
    testResult = ImageResolverTestUtils.toMap(tais)
    then:
    testResult == expectedMap
  }

}

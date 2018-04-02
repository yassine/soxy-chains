package com.github.yassine.soxychains.subsystem.docker.image.resolver

import com.google.common.collect.ImmutableMap
import com.google.common.io.Files
import com.google.inject.Guice
import com.google.inject.Injector
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.io.IOUtils
import spock.lang.Shared
import spock.lang.Specification

class FileResolverSpec extends Specification {

  @Shared
  DockerImageResourceResolver resolver

  def setupSpec() {
    Injector injector = Guice.createInjector();
    resolver = injector.getInstance(FileResolver.class)
  }

  def "It should create a tar archive of the docker image resources used to build the Docker image from a directory" () {
    setup:
      def expectedMap = [Dockerfile: 'nginx', 'config/app.config':'${no-change}', 'config/level2/level2':'nginx']
      def testResult
      def tempDir = Files.createTempDir()
      tempDir.deleteOnExit()
      FastClasspathScanner scanner = new FastClasspathScanner(getClass().getPackage().getName())
      scanner.matchFilenamePattern(getClass().getPackage().getName().replaceAll("\\.","/")+"/test_image.*", (FileMatchProcessor) {
        relativePath , InputStream inputStream, lengthBytes ->
          String filePath = relativePath.replaceAll(getClass().getPackage().getName().replaceAll("\\.","/")+"/test_image/","")
          File file = new File(tempDir.getAbsolutePath()+"/"+filePath)
          file.getParentFile().mkdirs()
          FileOutputStream fos = new FileOutputStream(file)
          IOUtils.copy(inputStream, fos)
          fos.flush()
          fos.close()
      })
      scanner.scan()
    when:
    InputStream is = resolver.resolve(tempDir.getAbsolutePath(), ImmutableMap.of("image","nginx"))
    TarArchiveInputStream tais = new TarArchiveInputStream(is)
    testResult = ImageResolverTestUtils.toMap(tais)
    then:
    testResult == expectedMap
  }

}

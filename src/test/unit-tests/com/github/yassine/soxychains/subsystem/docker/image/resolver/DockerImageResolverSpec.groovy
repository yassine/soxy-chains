package com.github.yassine.soxychains.subsystem.docker.image.resolver

import com.github.yassine.soxychains.subsystem.docker.DockerModule
import com.github.yassine.soxychains.subsystem.layer.LayerModule
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin
import com.google.common.collect.ImmutableMap
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.multibindings.Multibinder
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.io.IOUtils
import spock.guice.UseModules
import spock.lang.Specification

@UseModules([DockerModule, TestModule, LayerModule])
class DockerImageResolverSpec extends Specification {
  @Inject
  DockerImageResolver resolver

  def "resolve: it should be able to load templates from a classpath uri"() {
    setup:
    def expectedMap = [Dockerfile: 'nginx', 'config/app.config':'${no-change}', 'config/level2/level2':'nginx']
    def testResult
    when:
    InputStream is = resolver.resolve(URI.create("classpath://com/github/yassine/soxychains/subsystem/docker/image/resolver/test_image"), ImmutableMap.of("image","nginx"))
    TarArchiveInputStream tais = new TarArchiveInputStream(is)
    testResult = ImageResolverTestUtils.toMap(tais)
    then:
    testResult == expectedMap
  }

  def "resolve: it should be able to load templates from a file of the filesystem"() {
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
    InputStream is = resolver.resolve(URI.create("file://"+tempDir.getAbsolutePath()), ImmutableMap.of("image","nginx"))
    TarArchiveInputStream tais = new TarArchiveInputStream(is)
    testResult = ImageResolverTestUtils.toMap(tais)
    then:
    testResult == expectedMap
  }
  def "resolve: it should throw an exception for unsupported uri schemes"() {
    when:
    resolver.resolve(URI.create("http://github.com/yassine"), ImmutableMap.of())
    then:
    Exception e = thrown()
    e instanceof RuntimeException
  }

  static class TestModule extends AbstractModule {

    @Override
    protected void configure() {
      Multibinder<ServicesPlugin> multibinder = Multibinder.newSetBinder(binder(), ServicesPlugin.class);
    }
  }

}

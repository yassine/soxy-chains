package com.github.yassine.soxychains.cli

import com.github.yassine.soxychains.SoxyChainsApplication
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.TestUtils
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.image.api.DockerImage
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.reactivex.Observable
import org.apache.commons.io.IOUtils
import spock.guice.UseModules
import spock.lang.Specification
import spock.lang.Stepwise

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceImage
import static java.util.Arrays.stream

@Stepwise @UseModules(TestModule)
class InstallCommandSpec extends Specification {

  @Inject
  private Set<ImageRequirer> imageRequirers
  @Inject
  private DockerConfiguration configuration

  def "it should create docker images for required services"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("install", "-c", config.getAbsolutePath())
    def dockerClient  = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages  = dockerClient.listImagesCmd().exec()
    expect:
    //all the required images are created
    Observable.fromIterable(imageRequirers)
      .flatMap{ requirer -> requirer.require() }
      .map{ DockerImage requiredImage -> nameSpaceImage(configuration, requiredImage.getName()) }
      .map{ String requiredImageName -> dockerImages.stream().anyMatch{ repoImage ->
          stream(repoImage.getRepoTags()).anyMatch{tag -> tag.contains(requiredImageName)}
        }
      }
      .reduce(true, { a, b -> a && b })
      .blockingGet()
  }

  def "it should remove all the docker images required by the platform"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("uninstall", "-c", config.getAbsolutePath())
    def dockerClient = TestUtils.dockerClient(configuration.getHosts().get(0))
    def dockerImages  = dockerClient.listImagesCmd().exec()

    expect:
    Observable.fromIterable(imageRequirers)
      .flatMap{ requirer -> requirer.require() }
      .map{ DockerImage requiredImage -> nameSpaceImage(configuration, requiredImage.getName()) }
      .map{ String requiredImageName -> dockerImages.stream().allMatch{ repoImage ->
        stream((String []) repoImage.getRepoTags()).noneMatch{tag -> tag.contains(nameSpaceImage(configuration, requiredImageName))}
      }}
      .reduce(true, { a, b -> a && b })
      .blockingGet()
  }

  static class TestModule extends AbstractModule{
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("config-install.yaml")
      install(new SoxyChainsModule(is))
    }
  }

}

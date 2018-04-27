package com.github.yassine.soxychains.cli

import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.web.WebAPIConfiguration
import com.github.yassine.soxychains.web.WebAPIModule
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.rapidoid.config.Conf
import org.rapidoid.env.Env
import org.rapidoid.setup.App
import org.rapidoid.setup.Setup
import spock.guice.UseModules
import spock.lang.Specification

import static com.github.yassine.soxychains.cli.Application.main

@UseModules(TestModule)
class ServeCommandSpec extends Specification {

  @Inject
  private WebAPIConfiguration apiConfiguration

  def "run: it should start the web server" () {
    setup:
    File workDir = Files.createTempDir()
    File config = new File(workDir, "config.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-install.yaml"), new FileOutputStream(config))
    main("serve", "-c", config.getAbsolutePath())
    Thread.sleep(5000L)

    def responseNotFound = new OkHttpClient.Builder().build()
      .newCall(new Request.Builder()
      .url(String.format("http://%s:%s/404", apiConfiguration.getBindAddress(), apiConfiguration.getPort()))
      .get().build())
      .execute()

    expect:
    responseNotFound.code() == 404
  }

  def cleanupSpec(){
    App.shutdown()
    Setup.shutdownAll()
    Env.reset()
    App.resetGlobalState()
    Conf.reset()
    Conf.ROOT.reset()
  }

  static class TestModule extends AbstractModule{
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("config-install.yaml")
      install(new ConfigurationModule(is))
      install(new SoxyChainsModule())
      install(new WebAPIModule())
    }
  }

}

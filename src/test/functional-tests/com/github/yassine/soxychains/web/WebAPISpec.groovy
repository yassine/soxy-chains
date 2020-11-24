package com.github.yassine.soxychains.web

import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsModule
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Injector
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static io.reactivex.Observable.fromCallable

@UseModules(TestModule) @Stepwise
class WebAPISpec extends Specification {

  @Inject @Shared
  private WebAPI webAPI
  @Inject
  private WebAPIConfiguration apiConfiguration
  @Inject @Shared
  private Injector injector

  def "It should start the api server as per configuration"() {
    when:
    fromCallable({  -> webAPI.startup(); return true }).onErrorResumeNext(Observable.empty()).subscribeOn(Schedulers.io())
      .subscribe()
    Thread.sleep(5000L)
    def responseNotFound = new OkHttpClient.Builder().build()
      .newCall(new Request.Builder()
      .url(String.format("http://%s:%s/404", apiConfiguration.getBindAddress(), apiConfiguration.getPort()))
      .get().build())
      .execute()
    then:
    responseNotFound.code() == 404
  }

  def "Stop: It should stop the api server"() {
    when:
    webAPI.stop()
    new OkHttpClient.Builder().build()
      .newCall(new Request.Builder()
      .url(String.format("http://%s:%s/this-should-really-not-exist-404", apiConfiguration.getBindAddress(), apiConfiguration.getPort()))
      .get().build())
      .execute()
    then:
    ConnectException e = thrown()
    e != null
  }

  def cleanupSpec (){
    webAPI.stop()
  }

  static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      InputStream is = getClass().getResourceAsStream("config-web.yaml")
      install(new ConfigurationModule(is))
      install(new SoxyChainsModule())
      install(new WebAPIModule())
    }
  }

}

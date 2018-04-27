package com.github.yassine.soxychains.web.resource

import com.github.yassine.soxychains.web.WebAPI
import com.github.yassine.soxychains.web.WebAPIConfiguration
import com.google.inject.Inject
import com.google.inject.Injector
import io.reactivex.schedulers.Schedulers
import org.hamcrest.Matchers
import org.rapidoid.config.Conf
import org.rapidoid.env.Env
import org.rapidoid.setup.App
import org.rapidoid.setup.Setup
import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static io.reactivex.Observable.fromFuture
import static io.restassured.RestAssured.given
import static java.lang.String.format
import static java.util.concurrent.CompletableFuture.supplyAsync

@UseModules(ResourceTestModule) @Stepwise
class LayerResourceSpec extends Specification {

  @Inject @Shared
  private WebAPI webAPI
  @Inject @Shared
  private Injector injector
  @Inject
  private WebAPIConfiguration apiConfiguration

  def setupSpec(){
    fromFuture(supplyAsync({  -> webAPI.startup(); return true })).subscribeOn(Schedulers.io())
      .subscribe()
    Thread.sleep(5000L)
  }
  def cleanupSpec (){
    webAPI.stop()
  }

  def "get: it should return the list of online hosts"() {
    expect:
    given().port(apiConfiguration.getPort())
      .when()
      .get(format("http://%s:%s/layer", apiConfiguration.getBindAddress(), apiConfiguration.getPort()))
      .then()
      .statusCode(200)
      .body('$.size()', Matchers.equalTo(2))
  }
}

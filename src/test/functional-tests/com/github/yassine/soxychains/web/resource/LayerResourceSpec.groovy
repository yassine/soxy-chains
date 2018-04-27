package com.github.yassine.soxychains.web.resource

import com.github.yassine.soxychains.web.WebAPI
import com.github.yassine.soxychains.web.WebAPIConfiguration
import com.google.inject.Inject
import com.google.inject.Injector
import org.hamcrest.Matchers
import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static io.restassured.RestAssured.given
import static java.lang.String.format

@UseModules(ResourceTestModule) @Stepwise
class LayerResourceSpec extends Specification {

  @Inject @Shared
  private WebAPI webAPI
  @Inject @Shared
  private Injector injector
  @Inject
  private WebAPIConfiguration apiConfiguration

  def "get: it should return the list of online hosts"() {
    expect:
    given().port(apiConfiguration.getPort())
      .when()
      .get(format("http://%s:%s/layer", apiConfiguration.getBindAddress(), apiConfiguration.getPort()))
      .then()
      .statusCode(200)
      .body('$.size()', Matchers.equalTo(2))
  }

  def cleanupSpec (){
    webAPI.stop()
  }
}

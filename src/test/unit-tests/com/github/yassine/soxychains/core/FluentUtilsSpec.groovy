package com.github.yassine.soxychains.core

import spock.lang.Specification

import static com.github.yassine.soxychains.core.FluentUtils.getWithRetry

class FluentUtilsSpec extends Specification {

  def "getWithRetry: it should throw an exception if the calls fails after timeout"() {
    when:
    getWithRetry( { a -> this.justKeepFailing(); return true}, null, null  , 1, 1000)
      .blockingGet()
    then:
    SoxyChainsException e = thrown()
    e instanceof SoxyChainsException
  }


  def "getWithRetry: it should give back the result if it succeeds"() {
    when:
    def result = getWithRetry( { a ->  return "success"}, null, null  , 1, 1000)
      .blockingGet()
    then:
    result == "success"
  }

  def justKeepFailing(){
    throw new RuntimeException();
  }
}

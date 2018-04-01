package com.github.yassine.soxychains.subsystem.docker.config.validation

import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.ProvisionException
import spock.lang.Specification

import javax.validation.ConstraintViolationException

class ValidTLSConfigValidatorSpec extends Specification {

  def "A docker host configuration should provide a tls cert path when using tls"() {
    setup:
    InputStream config = getClass().getResourceAsStream("invalid-host-tls-config.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    DockerConfiguration configuration = injector.getInstance(DockerConfiguration.class)
    then:
    ProvisionException ex = thrown()
    ex.getErrorMessages().stream()
        .map{message -> message.getCause()}
        .filter{e -> e instanceof ConstraintViolationException}
        .map{e -> (ConstraintViolationException) e}
        .flatMap{e -> e.getConstraintViolations().stream()}
        .filter{ violation -> violation.messageTemplate.contains(ValidTLSConfigConstraint.class.getName()+".message") }
        .findAny().isPresent()
  }

  def "A valid docker host configuration should pass the validation"() {
    setup:
    InputStream config = getClass().getResourceAsStream("valid-host-tls-config.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    DockerConfiguration configuration = injector.getInstance(DockerConfiguration.class)
    then:
    true
  }

}

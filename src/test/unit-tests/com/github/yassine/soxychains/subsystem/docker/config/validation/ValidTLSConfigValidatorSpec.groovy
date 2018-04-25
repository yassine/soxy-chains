package com.github.yassine.soxychains.subsystem.docker.config.validation

import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.ProvisionException
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

class ValidTLSConfigValidatorSpec extends Specification {

  def "A docker host configuration should provide a tls cert path when using tls"() {
    setup:
    InputStream config = getClass().getResourceAsStream("invalid-host-tls-config.yaml")
    when:
    Injector injector = Guice.createInjector(new ConfigurationModule(config), new SoxyChainsModule())
    injector.getInstance(DockerContext.class)
    then:
    ProvisionException ex = thrown()
    ex.getErrorMessages().stream()
      .map{message -> ex.getCause().getCause()}
      .filter{e -> e instanceof ConstraintViolationException}
      .map{e -> (ConstraintViolationException) e}
      .flatMap{ ConstraintViolationException e -> e.getConstraintViolations().stream() }
      .filter{ ConstraintViolation violation -> violation.messageTemplate.contains(ValidTLSConfigConstraint.class.getName()+".message") }
      .findAny().isPresent()
  }

  def "A valid docker host configuration should pass the validation"() {
    setup:
    InputStream config = getClass().getResourceAsStream("valid-host-tls-config.yaml")
    when:
    Injector injector = Guice.createInjector(new ConfigurationModule(config), new SoxyChainsModule())
    DockerContext dockerContext = injector.getInstance(DockerContext.class)
    then:
    true
    dockerContext != null
  }

}

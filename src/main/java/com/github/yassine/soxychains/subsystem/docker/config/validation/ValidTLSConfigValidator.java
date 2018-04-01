package com.github.yassine.soxychains.subsystem.docker.config.validation;


import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidTLSConfigValidator implements ConstraintValidator<ValidTLSConfigConstraint, DockerHostConfiguration> {

  @Override
  public boolean isValid(DockerHostConfiguration hostConfiguration, ConstraintValidatorContext constraintValidatorContext) {
    return !hostConfiguration.getUsesTLS() || !StringUtils.isEmpty(hostConfiguration.getCertPath());
  }

}

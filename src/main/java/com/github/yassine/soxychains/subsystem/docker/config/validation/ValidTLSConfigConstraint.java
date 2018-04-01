package com.github.yassine.soxychains.subsystem.docker.config.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = ValidTLSConfigValidator.class)
@Retention(RetentionPolicy.RUNTIME) @Target({ TYPE, ANNOTATION_TYPE })
public @interface ValidTLSConfigConstraint {
  String message() default "{com.github.yassine.soxychains.subsystem.docker.config.validation.ValidTLSConfigConstraint.message}";
  Class<?>[] groups() default { };
  Class<? extends Payload>[] payload() default { };
}

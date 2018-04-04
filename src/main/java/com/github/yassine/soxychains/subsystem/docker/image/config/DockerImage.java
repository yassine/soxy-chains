package com.github.yassine.soxychains.subsystem.docker.image.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonTypeInfo(
  use      = JsonTypeInfo.Id.NAME,
  include  = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = RemoteDockerImage.class, name = "remote"),
  @JsonSubTypes.Type(value = FloatingDockerImage.class, name = "lambda")
})
@Getter @EqualsAndHashCode(of = "name") @RequiredArgsConstructor
public abstract class DockerImage {
  @NotNull
  protected final String name;
}

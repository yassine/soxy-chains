package com.github.yassine.soxychains.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;
import static com.google.common.collect.ImmutableSet.copyOf;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class TaskLoaderSupport implements TaskLoader {

  private final Injector injector;

  @Override
  public Set<Task> load() {
    return copyOf(loadSPIClasses(Task.class).stream()
            .map(injector::getInstance)
            .collect(Collectors.toList()));
  }
}

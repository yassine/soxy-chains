package com.github.yassine.soxychains.core;

import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class TaskScheduleProviderSupport implements TaskScheduleProvider {

  private final TaskScheduler taskScheduler;
  private final TaskLoader taskLoader;

  private LoadingCache<Phase,List<Set<Task>>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<Phase,List<Set<Task>>>() {
    @Override
    @SuppressWarnings({"unchecked", "NullableProblems"})
    public List<Set<Task>> load(Phase key){
      return taskScheduler.scheduleInstances(taskLoader.load().stream()
                .filter(task -> task.getClass().isAnnotationPresent(RunOn.class))
                .filter(task -> task.getClass().getAnnotation(RunOn.class).value().equals(key))
                .collect(toSet()));
    }
  });

  @Override @SneakyThrows
  public List<Set<Task>> get(Phase phase) {
    return CACHE.get(phase);
  }

}

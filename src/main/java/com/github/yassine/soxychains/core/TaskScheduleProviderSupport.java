package com.github.yassine.soxychains.core;

import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Set;

import static com.github.yassine.artifacts.guice.utils.GuiceUtils.loadSPIClasses;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
class TaskScheduleProviderSupport implements TaskScheduleProvider {

  private final Injector injector;
  private final TaskScheduler taskScheduler;

  private LoadingCache<Phase,List<Set<Task>>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<Phase,List<Set<Task>>>() {
    @Override
    @SuppressWarnings({"unchecked", "NullableProblems"})
    public List<Set<Task>> load(Phase key){
      Set<Task> tasks = loadSPIClasses(Task.class).stream()
        .filter(clazz -> clazz.isAnnotationPresent(RunOn.class))
        .filter(clazz -> clazz.getAnnotation(RunOn.class).value().equals(key))
        .map(injector::getInstance)
        .collect(toSet());
      return taskScheduler.scheduleInstances(tasks);
    }
  });

  @Override @SneakyThrows
  public List<Set<Task>> get(Phase phase) {
    return CACHE.get(phase);
  }

}

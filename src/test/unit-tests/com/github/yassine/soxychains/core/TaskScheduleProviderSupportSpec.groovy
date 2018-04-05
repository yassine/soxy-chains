package com.github.yassine.soxychains.core

import com.github.yassine.artifacts.guice.scheduling.DependsOn
import com.github.yassine.artifacts.guice.scheduling.TaskSchedulerModule
import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.reactivex.Single
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(TestModule)
class TaskScheduleProviderSupportSpec extends Specification {

  @Inject
  TaskScheduleProvider taskScheduleProvider

  def "it should 'waves' of tasks that can run in parallel and corresponds to a given phase" () {
    given:
    List<Set<Task>> waves = taskScheduleProvider.get(Phase.INSTALL)
    expect:
    waves.get(0) == [TestModule.taskA] as Set<Task>
    waves.get(1) == [TestModule.taskB, TestModule.taskC] as Set<Task>
  }

  static class TestModule extends AbstractModule {
    static TaskA taskA = new TaskA()
    static TaskB taskB = new TaskB()
    static TaskC taskC = new TaskC()
    static TaskD taskD = new TaskD()
    @Override
    protected void configure() {
      install(new TaskSchedulerModule())
      bind(TaskLoader.class).toInstance(new TaskLoader() {
        @Override
        Set<Task> load() {
          return [taskA, taskB, taskC, taskD] as Set<Task>
        }
      })
      bind(TaskScheduleProvider.class).to(TaskScheduleProviderSupport.class)
    }
  }

  @RunOn(Phase.INSTALL)
  static class TaskA implements Task{
    @Override
    Single<Boolean> execute() {
      return Single.just(true)
    }
  }
  @RunOn(Phase.INSTALL) @DependsOn(TaskA)
  static class TaskB  implements Task{
    @Override
    Single<Boolean> execute() {
      return Single.just(true)
    }
  }
  @RunOn(Phase.INSTALL) @DependsOn(TaskA)
  static class TaskC  implements Task{
    @Override
    Single<Boolean> execute() {
      return Single.just(true)
    }
  }
  @RunOn(Phase.UNINSTALL)
  static class TaskD  implements Task{
    @Override
    Single<Boolean> execute() {
      return Single.just(true)
    }
  }
}

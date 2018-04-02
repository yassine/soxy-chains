package com.github.yassine.soxychains.core;

import java.util.List;
import java.util.Set;

public interface TaskScheduleProvider {
  List<Set<Task>> get(Phase phase);
}

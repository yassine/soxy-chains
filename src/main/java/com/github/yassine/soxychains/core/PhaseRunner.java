package com.github.yassine.soxychains.core;

import io.reactivex.Single;

public interface PhaseRunner {
  Single<Boolean> runPhase(Phase phase);
}

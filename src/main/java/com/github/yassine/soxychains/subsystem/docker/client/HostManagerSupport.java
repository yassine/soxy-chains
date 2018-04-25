package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import static com.github.yassine.soxychains.core.FluentUtils.runAndGetAsMaybe;
import static io.reactivex.Observable.fromIterable;
import static io.reactivex.Single.just;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class HostManagerSupport implements HostManager {

  private final SoxyChainsContext context;
  private final DockerProvider dockerProvider;

  @Override
  public Observable<DockerHostConfiguration> list() {
    return fromIterable(context.getDocker().getHosts())
      .flatMapSingle(host -> isOnline(host).map(online -> Pair.of(host, online)))
      .filter(Pair::getValue)
      .map(Pair::getKey);
  }

  private Single<Boolean> isOnline(DockerHostConfiguration dockerHostConfiguration){
    return runAndGetAsMaybe(() -> dockerProvider.getClient(dockerHostConfiguration).pingCmd().exec(), true)
      .toSingle(false)
      .onErrorResumeNext(just(false))
      .subscribeOn(Schedulers.io());
  }

}

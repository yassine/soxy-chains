package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.service.gobetween.GobetweenConfiguration.GOBETWEEN_CONFIG_ID;

@RequiresImage(name = GOBETWEEN_CONFIG_ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+ GOBETWEEN_CONFIG_ID)
@AutoService(ServicesPlugin.class) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class GobetweenService implements ServicesPlugin<GobetweenConfiguration> {
  private final GobetweenConfiguration configuration;
}

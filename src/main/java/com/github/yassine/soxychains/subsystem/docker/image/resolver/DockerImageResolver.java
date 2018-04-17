package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.github.yassine.soxychains.core.SoxyChainsException;
import com.google.inject.Inject;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static java.lang.String.format;

public class DockerImageResolver {

  @Inject @ClassPath
  private DockerImageResourceResolver classPathResolver;
  @Inject @FileSystem
  private DockerImageResourceResolver fileSystemResolver;

  public InputStream resolve(URI uri, Map<String, ?> context){
    switch (ResolverScheme.schemeValueOf(uri.getScheme())){
      case CLASSPATH:
        return classPathResolver.resolve(uri.getHost()+uri.getRawPath(), context);
      case FILESYSTEM:
        return fileSystemResolver.resolve(uri.getRawPath(), context);
      default:
        throw new SoxyChainsException(format("Unsupported Scheme %s", uri.getScheme()));
    }
  }

}

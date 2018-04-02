package com.github.yassine.soxychains.subsystem.docker.image.resolver;

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

  private static final String CLASSPATH_SCHEME  = "classpath";
  private static final String FILESYSTEM_SCHEME = "file";

  public InputStream resolve(URI uri, Map<String, ?> context){
    switch (uri.getScheme()){
      case CLASSPATH_SCHEME:
        return classPathResolver.resolve(uri.getHost()+uri.getRawPath(), context);
      case FILESYSTEM_SCHEME:
        return fileSystemResolver.resolve(uri.getRawPath(), context);
      default:
        throw new RuntimeException(format("Unsupported Scheme %s", uri.getScheme()));
    }
  }

}

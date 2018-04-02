package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.util.HashMap;
import java.util.Map;

public class ImageResolverTestUtils {

  @SneakyThrows
  public static Map<String, String> toMap(TarArchiveInputStream tais){
    HashMap<String,String> testResult = new HashMap<>();
    ArchiveEntry entry = tais.getNextEntry();
    while(entry != null){
      testResult.put(entry.getName(),IOUtils.toString(tais, "utf-8"));
      entry = tais.getNextEntry();
    }
    return testResult;
  }

}

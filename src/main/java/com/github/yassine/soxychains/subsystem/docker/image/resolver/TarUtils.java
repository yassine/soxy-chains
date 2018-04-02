package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static com.machinezoo.noexception.Exceptions.sneak;

@Slf4j
public class TarUtils {

  public static InputStream createTARArchive(Map<String, String> entries){
    ByteArrayOutputStream bao  = new ByteArrayOutputStream();
    TarArchiveOutputStream tos = new TarArchiveOutputStream(bao);
    entries.forEach((key, value) -> sneak().run(() -> {
      TarArchiveEntry entry = new TarArchiveEntry(key);
      byte[] bytes = value.getBytes("utf-8");
      entry.setSize(bytes.length);
      tos.putArchiveEntry(entry);
      IOUtils.copy(new ByteArrayInputStream(bytes), tos);
      tos.closeArchiveEntry();
    }));
    return new ByteArrayInputStream(bao.toByteArray());
  }

}

package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.google.common.base.Joiner;
import freemarker.cache.TemplateLoader;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import java.io.*;
import java.util.*;

class ClassPathScanTemplateLoader implements TemplateLoader {

  private Map<String, byte[]> cache = Collections.synchronizedMap(new HashMap<>());

  @Override
  public Object findTemplateSource(String name){
    MutableObject<String> result = new MutableObject<>(null);
    String[] parts = name.split("/");
    ArrayList<String> partsList = new ArrayList<>(Arrays.asList(parts).subList(0, parts.length - 1));
    FastClasspathScanner scanner = new FastClasspathScanner(Joiner.on("/").join(partsList));
    scanner.matchFilenamePattern( name,
      (FileMatchProcessor) (relativePath, inputStream, lengthBytes) -> {
        if(inputStream.available() > 0){
          cache.put(name, IOUtils.toString(inputStream, "utf-8").getBytes());
          result.setValue(name);
        }
      });
    scanner.scan();

    return result.getValue();
  }

  @Override
  public long getLastModified(Object templateSource) {
    return -1;
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    if(templateSource instanceof String){
      return new InputStreamReader(new ByteArrayInputStream(cache.get(templateSource)), "utf-8");
    }
    return null;
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    if(templateSource instanceof InputStream){
      ((InputStream) templateSource).close();
    }
  }

}

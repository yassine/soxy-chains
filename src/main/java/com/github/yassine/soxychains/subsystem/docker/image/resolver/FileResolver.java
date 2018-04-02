package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.machinezoo.noexception.Exceptions.sneak;
import static java.nio.file.Files.find;

@Slf4j
public class FileResolver implements DockerImageResourceResolver {

  private final LoadingCache<String, Configuration> configLoader = CacheBuilder.newBuilder().build(new CacheLoader<String, Configuration>() {
    @Override
    public Configuration load(String key) throws Exception {
      Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);
      configuration.setTemplateLoader(new FileTemplateLoader(new File(key)));
      return configuration;
    }
  });

  @Override
  public InputStream resolve(String path, Map<String, ?> context) {
    return resolve(path, context, DEFAULT_INCLUDE_PREDICATE);
  }

  @Override @SneakyThrows
  public InputStream resolve(String path, Map<String, ?> context, Predicate<String> includePredicate) {
    Path contextPath = Paths.get(path);
    Configuration configuration = configLoader.get(path);
    Map<String,String> entries = find(contextPath, 20, (p, attr) -> true)
      .map(Path::toFile)
      .filter( file -> !file.isDirectory() )
      .filter( file -> includePredicate.test(file.getAbsolutePath()) )
      .map( filePath -> {
        String relativePath = filePath.getAbsolutePath().replaceAll(path+"/","");
        if ( DEFAULT_TEMPLATE_PREDICATE.test(filePath.getAbsolutePath()) ) {
          Template template = sneak().get(() -> configuration.getTemplate(relativePath));
          StringWriter sw = new StringWriter();
          sneak().run(() -> template.process(context, sw));
          sw.flush();
          String result = sw.toString();
          String entryName = relativePath;
          if(relativePath.endsWith(".template")){
            entryName = entryName.substring(0, entryName.length() - ".template".length());
          }else if(relativePath.endsWith(".tpl")){
            entryName = entryName.substring(0, entryName.length() - ".tpl".length());
          }
          return Pair.of(entryName, result);
        } else {
          return Pair.of(relativePath, sneak().get(() -> IOUtils.toString(sneak().get(() -> new FileInputStream(filePath)), "utf-8")));
        }
      }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    return TarUtils.createTARArchive(entries);
  }

}

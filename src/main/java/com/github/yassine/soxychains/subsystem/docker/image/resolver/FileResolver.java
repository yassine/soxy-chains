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
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.yassine.soxychains.subsystem.docker.image.resolver.ResolverUtils.DEFAULT_INCLUDE_PREDICATE;
import static com.github.yassine.soxychains.subsystem.docker.image.resolver.ResolverUtils.TEMPLATE_FILENAME_PATTERN;
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
    Stream<Path> files = find(contextPath, 20, (p, attr) -> true);
    Map<String,String> entries = files
      .map(Path::toFile)
      .filter( file -> !file.isDirectory() )
      .filter( file -> includePredicate.test(file.getAbsolutePath()) )
      .map( filePath -> {
        String absolutePath = filePath.getAbsolutePath();
        String entryFile    = sneak().get(() -> IOUtils.toString(new FileInputStream(filePath), "utf-8"));
        Matcher m           = TEMPLATE_FILENAME_PATTERN.matcher(absolutePath);
        boolean matching    = m.find();
        if(matching){
          absolutePath = absolutePath.substring(0, absolutePath.length() - m.group().length());
          StringWriter sw = new StringWriter();
          Template template = sneak().get(() -> configuration.getTemplate(filePath.getAbsolutePath().replaceAll(path+"/","")));
          sneak().run(() -> template.process(context, sw));
          sw.flush();
          entryFile = sw.toString();
          return Pair.of(absolutePath.replaceAll(path+"/",""), entryFile);
        }else{
          return Pair.of(absolutePath.replaceAll(path+"/",""), entryFile);
        }
      }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    files.close();
    return ResolverUtils.createTARArchive(entries);
  }

}

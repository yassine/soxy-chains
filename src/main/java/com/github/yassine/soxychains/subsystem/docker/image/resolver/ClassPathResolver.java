package com.github.yassine.soxychains.subsystem.docker.image.resolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class ClassPathResolver implements DockerImageResourceResolver{

  @Inject
  private ClassPathScanTemplateLoader templateLoader;

  private final LoadingCache<String, Configuration> configLoader = CacheBuilder.newBuilder().build(new CacheLoader<String, Configuration>() {
    @Override
    public Configuration load(String key) {
      Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);
      configuration.unsetTemplateLoader();
      configuration.setTemplateLoader(templateLoader);
      return configuration;
    }
  });

  @Override @SneakyThrows
  public InputStream resolve(String path, Map<String, ?> context, Predicate<String> includePredicate){
    Configuration configuration = configLoader.get("");
    FastClasspathScanner scanner = new FastClasspathScanner(path.replaceAll("/","."));
    ImmutableMap.Builder<String, String> sb = ImmutableMap.builder();
    scanner.matchFilenamePattern(path+"/.*",
      (FileMatchProcessor) (relativePath, inputStream, lengthBytes) -> {

        if( includePredicate.test(relativePath) ){
          String entryName = relativePath;
          String entryFile = IOUtils.toString(inputStream, "utf-8");
          if(relativePath.endsWith(".template")){
            entryName = relativePath.substring(0, relativePath.length() - ".template".length());
          }else if(relativePath.endsWith(".tpl")){
            entryName = relativePath.substring(0, relativePath.length() - ".tpl".length());
          }

          if(DEFAULT_TEMPLATE_PREDICATE.test(relativePath)){
            try{
              Template template = configuration.getTemplate(relativePath);
              StringWriter sw = new StringWriter();
              template.process(context, sw);
              sw.flush();
              entryFile = sw.toString();
            }catch (Exception e){
              log.error(e.getMessage(), e);
            }
          }
          sb.put(entryName.replaceAll(path+"/",""), entryFile);
        }
      });
    scanner.scan();
    return TarUtils.createTARArchive(sb.build());
  }

  public InputStream resolve(String path, Map<String, ?> context){
    return resolve(path, context, DEFAULT_INCLUDE_PREDICATE);
  }

}

package com.github.yassine.soxychains;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.yassine.soxychains.core.PluginsConfigDeserializer;
import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesConfiguration;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Strings;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class TestUtils {

  @SuppressWarnings("unchecked")
  public static ObjectMapper mapper() {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    om.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(ServicesConfiguration.class,
      new PluginsConfigDeserializer( ServicesPlugin.class,
        (map) -> new ServicesConfiguration((Map<Class<? extends Plugin<ServicesPluginConfiguration>>, ServicesPluginConfiguration>) map)));
    om.registerModule(simpleModule);
    return om;
  }

  public static  DockerClient dockerClient(DockerHostConfiguration hostConfiguration) {
    return DockerClientBuilder.getInstance(hostConfiguration.getUri().toString()).build();
  }

  public static boolean isTCPVPNConfiguration(String base64Configuration){
    return Arrays.stream(Strings.fromUTF8ByteArray(Base64.getDecoder().decode(base64Configuration)).split("\n"))
      .filter(StringUtils::isNotEmpty)
      .map(String::trim)
      .filter(s -> s.startsWith("proto"))
      .anyMatch(s -> s.contains("tcp"));
  }

  @SneakyThrows
  public static String findOnlineVPNConfiguration(){
    String responseString = new OkHttpClient.Builder().build()
      .newCall(new Request.Builder().url("http://130.158.75.33/api/iphone").build())
      .execute()
      .body()
      .string();
    String data = Joiner.on('\n').join(
      stream(responseString.split("\n"))
        .filter(line -> line.contains(","))
        .collect(Collectors.<String>toList())
    );
    Reader reader = new StringReader(data);
    CSVRecord serverRecord = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader).getRecords()
      .stream()
      .filter(record -> TestUtils.isTCPVPNConfiguration(record.get(record.size() - 1)))
      .collect(Collectors.toList())
      .get(0);
    return serverRecord.get(serverRecord.size() - 1);
  }

}

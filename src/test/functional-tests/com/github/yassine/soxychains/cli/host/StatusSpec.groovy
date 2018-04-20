package com.github.yassine.soxychains.cli.host

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.yassine.soxychains.SoxyChainsApplication
import com.google.common.io.Files
import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.util.stream.Collector
import java.util.stream.Collectors

import static java.util.Arrays.stream
class StatusSpec extends Specification {
  def "run: it should output the status of the docker hosts"() {
    setup:
    File workDir = Files.createTempDir()
    File config  = new File(workDir, "config.yaml")
    File output  = new File(workDir, "output.yaml")
    workDir.deleteOnExit()
    IOUtils.copy(getClass().getResourceAsStream("config-status.yaml"), new FileOutputStream(config))
    SoxyChainsApplication.main("host", "status", "-c", config.getAbsolutePath(), "-o", output.getAbsolutePath())
    ObjectMapper mapper = new ObjectMapper()
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    def outputStatuses = mapper.readValue(output, Status.HostStatus[].class)
    expect:
    stream(outputStatuses).filter({ status -> status.isUp()}).filter({status -> status.getHost() == null }).collect(Collectors.toList() as Collector<? super Status.HostStatus, Object, Object>).size() == 1
    stream(outputStatuses).filter({ status -> !status.isUp()}).collect(Collectors.toList() as Collector<? super Status.HostStatus, Object, Object>).size() == 1
    outputStatuses.length == 2
  }
}

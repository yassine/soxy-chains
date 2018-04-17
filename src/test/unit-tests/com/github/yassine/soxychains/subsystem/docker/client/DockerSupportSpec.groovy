package com.github.yassine.soxychains.subsystem.docker.client

import com.github.dockerjava.api.command.*
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Network
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

class DockerSupportSpec extends Specification {

  DockerConfiguration dockerSubsystemConfiguration = Mock()

  void setup() {
    dockerSubsystemConfiguration.getNamespace() >> "testing"
  }

  def "FindImageByTag : it should return the looked-up image"() {
    given:
    SoxyChainsDockerClient docker = Mock()
    ListImagesCmd listImagesCmd = Mock()
    Image targetImage = Mock()
    String[] repoTags = ["my-image"]
    targetImage.getRepoTags() >> repoTags
    List<Image> images = Arrays.asList(targetImage)
    listImagesCmd.exec() >> images
    docker.listImagesCmd() >> listImagesCmd
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)

    expect:
    !dockerHelperSupport.findImageByTag("my-image").isEmpty().blockingGet()
  }

  def "FindImageByTag : it should return no result when the image doesn't exist"() {
    given:
    SoxyChainsDockerClient docker = Mock()
    ListImagesCmd listImagesCmd = Mock()
    Image targetImage = Mock()
    String[] repoTags = ["my-other-image"]
    targetImage.getRepoTags() >> repoTags
    List<Image> images = Arrays.asList(targetImage)
    listImagesCmd.exec() >> images
    docker.listImagesCmd() >> listImagesCmd
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)

    when:
    def absent = dockerHelperSupport.findImageByTag("my-image").isEmpty().blockingGet()

    then:
    absent
  }

  def "CreateNetwork  : it should create a network when a no network with the same name exists already"() {

    given:
    SoxyChainsDockerClient docker = Mock()
    ListNetworksCmd listNetworksCmd = Mock()
    CreateNetworkCmd createNetworkCmd = Mock()
    CreateNetworkResponse createNetworkResponse = Mock()
    DockerHostConfiguration hostConfiguration = Mock()
    AtomicInteger ai = new AtomicInteger(0)
    docker.createNetworkCmd() >> createNetworkCmd
    createNetworkCmd.withName(_ as String) >> createNetworkCmd
    createNetworkResponse.getId() >> "STUBBED_ID"
    ArrayList<Integer> callTrace = new ArrayList<>()
    createNetworkCmd.exec() >> { args ->
      ai.incrementAndGet()
      callTrace.add(2)
      return createNetworkResponse
    }
    listNetworksCmd.exec()    >> new ArrayList<>()
    docker.configuration()    >> hostConfiguration
    docker.listNetworksCmd()  >> listNetworksCmd

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.createNetwork("my-network", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}).blockingGet()

    then:
    ai.get() == 1
    callTrace == [1, 2, 3]

  }

  def "CreateNetwork  : it should skip creating the network when a network with the same name exists already"() {

    given:
    SoxyChainsDockerClient docker = Mock()
    ListNetworksCmd listNetworksCmd = Mock()
    CreateNetworkCmd createNetworkCmd = Mock()
    Network targetNetwork = Mock()
    targetNetwork.getName() >> "my-network"
    targetNetwork.getId()   >> "STUBBED_ID"
    List<Network> networks = Arrays.asList(targetNetwork)
    CreateNetworkResponse createNetworkResponse = Mock()
    DockerHostConfiguration hostConfiguration = Mock()
    AtomicInteger ai = new AtomicInteger(0)
    docker.createNetworkCmd() >> createNetworkCmd
    createNetworkCmd.withName(_ as String) >> createNetworkCmd
    createNetworkResponse.getId() >> "STUBBED_ID"
    createNetworkCmd.exec() >> { args ->
      ai.incrementAndGet()
      return createNetworkResponse
    }
    docker.configuration()    >> hostConfiguration
    docker.listNetworksCmd()  >> listNetworksCmd
    listNetworksCmd.exec() >> networks
    ArrayList<Integer> callTrace = new ArrayList<>()

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.createNetwork("my-network", {test -> callTrace.add(1)}, {test -> callTrace.add(2)}).blockingGet()

    then:
    ai.get() == 0
    callTrace == []

  }

  def "RemoveNetwork  : it should remove a network when a the network with the same name exists"() {
    given:
    SoxyChainsDockerClient docker = Mock()
    ListNetworksCmd listNetworksCmd = Mock()
    RemoveNetworkCmd removeNetworkCmd = Mock()
    Network targetNetwork = Mock()
    targetNetwork.getName() >> "my-network"
    targetNetwork.getId() >> "STUBBED_ID"
    List<Network> networks = Arrays.asList(targetNetwork)
    CreateNetworkResponse createNetworkResponse = Mock()
    DockerHostConfiguration hostConfiguration = Mock()
    AtomicInteger ai = new AtomicInteger(0)
    docker.removeNetworkCmd(_ as String) >> removeNetworkCmd
    createNetworkResponse.getId() >> "STUBBED_ID"
    ArrayList<Integer> callTrace = new ArrayList<>()
    removeNetworkCmd.exec() >> { args ->
      ai.incrementAndGet()
      callTrace.add(2)
      return createNetworkResponse
    }
    docker.configuration()    >> hostConfiguration
    docker.listNetworksCmd()  >> listNetworksCmd
    listNetworksCmd.exec() >> networks

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.removeNetwork("my-network", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}).blockingGet()

    then:
    ai.get() == 1
    callTrace == [1, 2, 3]
  }

  def "RemoveNetwork  : it should avoid removing a network when a the network with the provided name does not exists"() {
    given:
    SoxyChainsDockerClient docker = Mock()
    ListNetworksCmd listNetworksCmd = Mock()
    RemoveNetworkCmd removeNetworkCmd = Mock()
    List<Network> networks = Arrays.asList()
    CreateNetworkResponse createNetworkResponse = Mock()
    DockerHostConfiguration hostConfiguration = Mock()
    AtomicInteger ai = new AtomicInteger(0)
    docker.removeNetworkCmd(_ as String) >> removeNetworkCmd
    createNetworkResponse.getId() >> "STUBBED_ID"
    removeNetworkCmd.exec() >> { args ->
      ai.incrementAndGet()
      return createNetworkResponse
    }
    docker.configuration()    >> hostConfiguration
    docker.listNetworksCmd()  >> listNetworksCmd
    listNetworksCmd.exec() >> networks
    ArrayList<Integer> callTrace = new ArrayList<>()

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.removeNetwork("my-network", {test -> callTrace.add(1)}, {test -> callTrace.add(2)}).blockingGet()

    then:
    ai.get() == 0
    callTrace == []
  }

  def "StartContainer : it should create & start container if the container does not already exist" () {
    given:
    AtomicInteger createContainerCounter = new AtomicInteger(0)
    AtomicInteger startContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    CreateContainerResponse createContainerResponse = Mock()
    SoxyChainsDockerClient docker         = Mock()
    ListContainersCmd listContainersCmd   = Mock()
    CreateContainerCmd createContainerCmd = Mock()
    StartContainerCmd startContainerCmd   = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    createContainerResponse.getId() >> "STUBBED_ID"
    createContainerCmd.withName(_ as String) >> createContainerCmd
    createContainerCmd.withLabels(_ as Map) >> createContainerCmd
    ArrayList<Integer> callTrace = new ArrayList<>()
    createContainerCmd.exec() >> { args ->
      createContainerCounter.incrementAndGet()
      callTrace.add(2)
      Container container = Mock()
      container.getStatus() >> ""
      container.getId() >> "STUBBED_ID"
      container.getNames() >> ['my-container']
      containers.add(container)
      return createContainerResponse
    }
    startContainerCmd.exec() >> { args ->
      startContainerCounter.incrementAndGet()
      callTrace.add(5)
      return startContainerCmd
    }
    docker.createContainerCmd(_ as String) >> createContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    docker.startContainerCmd(_ as String) >> startContainerCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.runContainer("my-container", "my-image", { test -> callTrace.add(1)}, { test -> callTrace.add(3)}, { test -> callTrace.add(4)}, { test -> callTrace.add(6)}).blockingGet()

    then:
    createContainerCounter.get() == 1
    startContainerCounter.get()  == 1
    callTrace == [1, 2, 3, 4, 5, 6]
  }

  def "StartContainer : it should skip container creation & start container if the container does exist but is stopped" () {
    given:
    AtomicInteger createContainerCounter = new AtomicInteger(0)
    AtomicInteger startContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    CreateContainerResponse createContainerResponse = Mock()
    SoxyChainsDockerClient docker         = Mock()
    ListContainersCmd listContainersCmd   = Mock()
    CreateContainerCmd createContainerCmd = Mock()
    StartContainerCmd startContainerCmd   = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    Container container                   = Mock()
    container.getStatus() >> ""
    container.getId() >> "STUBBED_ID"
    container.getNames() >> ["my-container"]
    containers.add(container)
    createContainerResponse.getId() >> "STUBBED_ID"
    createContainerCmd.withName(_ as String) >> createContainerCmd
    ArrayList<Integer> callTrace = new ArrayList<>()
    createContainerCmd.exec() >> { args ->
      createContainerCounter.incrementAndGet()
      callTrace.add(2)
      return createContainerResponse
    }
    startContainerCmd.exec() >> { args ->
      startContainerCounter.incrementAndGet()
      callTrace.add(5)
      return startContainerCmd
    }
    docker.createContainerCmd(_ as String) >> createContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    docker.startContainerCmd(_ as String) >> startContainerCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.runContainer("my-container", "my-image", { test -> callTrace.add(1)}, { test -> callTrace.add(3)}, { test -> callTrace.add(4)}, { test -> callTrace.add(6)}).blockingGet()

    then:
    createContainerCounter.get() == 0
    startContainerCounter.get()  == 1
    callTrace == [4,5,6]
  }

  def "StartContainer : it should skip container creation & startup if the container does exist & is running" () {
    given:
    AtomicInteger createContainerCounter = new AtomicInteger(0)
    AtomicInteger startContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    CreateContainerResponse createContainerResponse = Mock()
    SoxyChainsDockerClient docker             = Mock()
    ListContainersCmd listContainersCmd   = Mock()
    CreateContainerCmd createContainerCmd = Mock()
    StartContainerCmd startContainerCmd   = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    Container container                   = Mock()
    container.getStatus() >> "Up 3 hours"
    container.getId() >> "STUBBED_ID"
    containers.add(container)
    createContainerResponse.getId() >> "STUBBED_ID"
    createContainerCmd.withName(_ as String) >> createContainerCmd
    ArrayList<Integer> callTrace = new ArrayList<>()
    createContainerCmd.exec() >> { args ->
      createContainerCounter.incrementAndGet()
      return createContainerResponse
    }
    startContainerCmd.exec() >> { args ->
      startContainerCounter.incrementAndGet()
      return startContainerCmd
    }
    docker.createContainerCmd(_ as String) >> createContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    docker.startContainerCmd(_ as String) >> startContainerCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.runContainer("my-container", "my-image", { test -> callTrace.add(1)}, { test -> callTrace.add(2)}, { test -> callTrace.add(3)}, { test -> callTrace.add(4)}).blockingGet()

    then:
    createContainerCounter.get() == 0
    startContainerCounter.get()  == 0
    callTrace == []
  }

  def "StartContainer : it should return false if the container fails to be created" () {
    given:
    SoxyChainsDockerClient docker             = Mock()
    ListContainersCmd listContainersCmd   = Mock()
    CreateContainerCmd createContainerCmd = Mock()
    StartContainerCmd startContainerCmd   = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    createContainerCmd.withName(_ as String) >> createContainerCmd
    ArrayList<Integer> callTrace = new ArrayList<>()
    createContainerCmd.exec() >> { args ->
      throw new RuntimeException()
    }
    docker.createContainerCmd(_ as String) >> createContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    docker.startContainerCmd(_ as String) >> startContainerCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> []

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    boolean created = dockerHelperSupport.runContainer("my-container", "my-image", { test -> callTrace.add(1)}, { test -> callTrace.add(2)}, { test -> callTrace.add(3)}, { test -> callTrace.add(4)}).blockingGet()

    then:
    !created
  }

  def "StartContainer : it should return false if the container fails to start" () {
    given:
    List<Container> containers = new ArrayList<>()
    CreateContainerResponse createContainerResponse = Mock()
    SoxyChainsDockerClient docker         = Mock()
    ListContainersCmd listContainersCmd   = Mock()
    CreateContainerCmd createContainerCmd = Mock()
    StartContainerCmd startContainerCmd   = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    createContainerResponse.getId() >> "STUBBED_ID"
    createContainerCmd.withName(_ as String) >> createContainerCmd
    createContainerCmd.withLabels(_ as Map) >> createContainerCmd
    ArrayList<Integer> callTrace = new ArrayList<>()
    createContainerCmd.exec() >> { args ->
      Container container = Mock()
      container.getStatus() >> ""
      container.getId() >> "STUBBED_ID"
      containers.add(container)
      return createContainerResponse
    }
    startContainerCmd.exec() >> { args ->
      throw new RuntimeException()
    }
    docker.createContainerCmd(_ as String) >> createContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    docker.startContainerCmd(_ as String) >> startContainerCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    boolean started = dockerHelperSupport.runContainer("my-container", "my-image", { test -> callTrace.add(1)}, { test -> callTrace.add(2)}, { test -> callTrace.add(3)}, { test -> callTrace.add(4)}).blockingGet()

    then:
    !started
  }

  def "StopContainer : it should stop & remove container if the container exist and is running"() {
    given:
    AtomicInteger stopContainerCounter = new AtomicInteger(0)
    AtomicInteger removeContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    Container container = Mock()
    container.getStatus() >> "Up 3 hours"
    container.getId() >> "STUBBED_ID"
    container.getNames() >> ["my-container"]
    containers.add(container)
    ListContainersCmd listContainersCmd   = Mock()
    StopContainerCmd stopContainerCommand = Mock()
    RemoveContainerCmd removeContainerCmd = Mock()
    SoxyChainsDockerClient docker         = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    ArrayList<Integer> callTrace = new ArrayList<>()
    stopContainerCommand.exec() >> { args ->
      stopContainerCounter.incrementAndGet()
      callTrace.add(2)
      return stopContainerCommand
    }
    removeContainerCmd.exec() >> { args ->
      removeContainerCounter.incrementAndGet()
      callTrace.add(5)
      return removeContainerCmd
    }
    docker.stopContainerCmd(_ as String) >> stopContainerCommand
    docker.removeContainerCmd(_ as String) >> removeContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.stopContainer("my-container", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}, {test -> callTrace.add(4)}, {test -> callTrace.add(6)}).blockingGet()

    then:
    stopContainerCounter.get() == 1
    removeContainerCounter.get()  == 1
    callTrace == [1, 2, 3, 4, 5, 6]
  }

  def "StopContainer : it should skip stopping the container but removes it if the container exist and is not running"() {
    given:
    AtomicInteger stopContainerCounter = new AtomicInteger(0)
    AtomicInteger removeContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    Container container = Mock()
    container.getStatus() >> "Exited"
    container.getId() >> "STUBBED_ID"
    container.getNames() >> ["my-container"]
    containers.add(container)
    ListContainersCmd listContainersCmd   = Mock()
    StopContainerCmd stopContainerCommand = Mock()
    RemoveContainerCmd removeContainerCmd = Mock()
    SoxyChainsDockerClient docker             = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    ArrayList<Integer> callTrace = new ArrayList<>()
    stopContainerCommand.exec() >> { args ->
      stopContainerCounter.incrementAndGet()
      callTrace.add(2)
      return stopContainerCommand
    }
    removeContainerCmd.exec() >> { args ->
      removeContainerCounter.incrementAndGet()
      callTrace.add(5)
      return removeContainerCmd
    }
    docker.stopContainerCmd(_ as String) >> stopContainerCommand
    docker.removeContainerCmd(_ as String) >> removeContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.stopContainer("my-container", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}, {test -> callTrace.add(4)}, {test -> callTrace.add(6)}).blockingGet()

    then:
    stopContainerCounter.get() == 0
    removeContainerCounter.get()  == 1
    callTrace == [4,5,6]
  }

  def "StopContainer : it should skip container stopping & removal if the container does not exist"() {
    given:
    AtomicInteger stopContainerCounter = new AtomicInteger(0)
    AtomicInteger removeContainerCounter  = new AtomicInteger(0)
    List<Container> containers = new ArrayList<>()
    ListContainersCmd listContainersCmd   = Mock()
    StopContainerCmd stopContainerCommand = Mock()
    RemoveContainerCmd removeContainerCmd = Mock()
    SoxyChainsDockerClient docker             = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    stopContainerCommand.exec() >> { args ->
      stopContainerCounter.incrementAndGet()
      return stopContainerCommand
    }
    removeContainerCmd.exec() >> { args ->
      removeContainerCounter.incrementAndGet()
      return removeContainerCmd
    }
    docker.stopContainerCmd(_ as String) >> stopContainerCommand
    docker.removeContainerCmd(_ as String) >> removeContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers
    ArrayList<Integer> callTrace = new ArrayList<>()

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    dockerHelperSupport.stopContainer("my-container", {test -> callTrace.add(1)}, {test -> callTrace.add(2)}, {test -> callTrace.add(3)}, {test -> callTrace.add(4)}).blockingGet()

    then:
    stopContainerCounter.get() == 0
    removeContainerCounter.get()  == 0
    callTrace == []
  }

  def "StopContainer : it should return false if the container fails to stop"() {
    given:
    List<Container> containers = new ArrayList<>()
    Container container = Mock()
    container.getStatus() >> "Up 3 hours"
    container.getId() >> "STUBBED_ID"
    containers.add(container)
    ListContainersCmd listContainersCmd   = Mock()
    StopContainerCmd stopContainerCommand = Mock()
    RemoveContainerCmd removeContainerCmd = Mock()
    SoxyChainsDockerClient docker         = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    ArrayList<Integer> callTrace = new ArrayList<>()
    stopContainerCommand.exec() >> { args ->
      throw new RuntimeException()
    }
    docker.stopContainerCmd(_ as String) >> stopContainerCommand
    docker.removeContainerCmd(_ as String) >> removeContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    boolean stopped = dockerHelperSupport.stopContainer("my-container", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}, {test -> callTrace.add(4)}, {test -> callTrace.add(6)}).blockingGet()

    then:
    !stopped
  }

  def "StopContainer : it should return false if the container fails to get removed"() {
    given:
    List<Container> containers = new ArrayList<>()
    Container container = Mock()
    container.getStatus() >> "Up 3 hours"
    container.getId() >> "STUBBED_ID"
    containers.add(container)
    ListContainersCmd listContainersCmd   = Mock()
    StopContainerCmd stopContainerCommand = Mock()
    RemoveContainerCmd removeContainerCmd = Mock()
    SoxyChainsDockerClient docker         = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    ArrayList<Integer> callTrace = new ArrayList<>()
    stopContainerCommand.exec() >> { args ->
      return stopContainerCommand
    }
    removeContainerCmd.exec() >> { args ->
      throw new RuntimeException()
    }
    docker.stopContainerCmd(_ as String) >> stopContainerCommand
    docker.removeContainerCmd(_ as String) >> removeContainerCmd
    docker.configuration()      >> hostConfiguration
    docker.listContainersCmd()  >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String) >> listContainersCmd
    listContainersCmd.withShowAll(_ as Boolean) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as String[]) >> listContainersCmd
    listContainersCmd.withLabelFilter(_ as Map<String, String>) >> listContainersCmd
    listContainersCmd.exec()    >> containers

    when:
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
    boolean removed = dockerHelperSupport.stopContainer("my-container", {test -> callTrace.add(1)}, {test -> callTrace.add(3)}, {test -> callTrace.add(4)}, {test -> callTrace.add(6)}).blockingGet()

    then:
    !removed

  }

  def "RemoveImage   : When the image exists the image shall be removed and the result shall be true"() {
    given:
    SoxyChainsDockerClient docker = Mock()
    ListImagesCmd listImagesCmd = Mock()
    RemoveImageCmd removeImageCmd = Mock()
    DockerHostConfiguration hostConfiguration   = Mock()
    AtomicInteger removeImageCounter = new AtomicInteger(0)
    ArrayList<Integer> callTrace = Collections.synchronizedList(new ArrayList<>())
    removeImageCmd.withForce(_ as Boolean) >> removeImageCmd
    removeImageCmd.exec() >> { args ->
      removeImageCounter.incrementAndGet()
      callTrace.add(2)
      return removeImageCmd
    }
    Image targetImage = Mock()
    targetImage.getId() >> "IMAGE_ID"
    String[] repoTags = ["my-image"]
    targetImage.getRepoTags() >> repoTags
    List<Image> images = Arrays.asList(targetImage)
    listImagesCmd.exec()   >> images
    docker.listImagesCmd() >> listImagesCmd
    docker.removeImageCmd(_ as String) >> removeImageCmd
    docker.configuration() >> hostConfiguration
    DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)

    when:
    def result = dockerHelperSupport.removeImage("my-image",
        {test -> callTrace.add(1)},
        {test -> callTrace.add(3)})
                .blockingGet()

    then:
    callTrace == [1, 2, 3]
    removeImageCounter.get() == 1
    result
  }

  def "RemoveImage : When the image does not exist the result shall be true"() {
    given:
      SoxyChainsDockerClient docker = Mock()
      ListImagesCmd listImagesCmd = Mock()
      RemoveImageCmd removeImageCmd = Mock()
      DockerHostConfiguration hostConfiguration   = Mock()
      AtomicInteger removeImageCounter = new AtomicInteger(0)
      removeImageCmd.exec() >> { args ->
        removeImageCounter.incrementAndGet()
        return removeImageCmd
      }
      List<Image> images = new ArrayList<>()
      listImagesCmd.exec()   >> images
      docker.listImagesCmd() >> listImagesCmd
      docker.removeImageCmd(_ as String) >> removeImageCmd
      docker.configuration() >> hostConfiguration
      DockerSupport dockerHelperSupport = new DockerSupport(docker, dockerSubsystemConfiguration)
      ArrayList<Integer> callTrace = new ArrayList<>()

    when:
      def result = dockerHelperSupport.removeImage(
        "my-image",
        {test -> callTrace.add(1)},
         {test -> callTrace.add(2)}
        ).blockingGet(true)
    then:
      callTrace == []
      removeImageCounter.get() == 0
      result
  }

}

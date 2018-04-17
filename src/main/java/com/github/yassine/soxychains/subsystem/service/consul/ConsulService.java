package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.CONSUL_CONFIG_ID;

@RequiresImage(name = CONSUL_CONFIG_ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+ CONSUL_CONFIG_ID)
@AutoService(ServicesPlugin.class) @ConfigKey(CONSUL_CONFIG_ID)
public class ConsulService implements ServicesPlugin<ConsulConfiguration>{
}

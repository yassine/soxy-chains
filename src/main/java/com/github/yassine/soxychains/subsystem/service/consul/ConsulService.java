package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.subsystem.service.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.ID;

@RequiresImage(name = ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+ ID)
@AutoService(ServicesPlugin.class)
public class ConsulService implements ServicesPlugin<ConsulConfiguration>{
}

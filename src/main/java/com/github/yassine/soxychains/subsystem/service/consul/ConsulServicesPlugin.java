package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

@AutoService(ServicesPlugin.class)
public class ConsulServicesPlugin implements ServicesPlugin<ConsulServicesPluginConfiguration> {
}

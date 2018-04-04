package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

@AutoService(ServicesPlugin.class) @ConfigKey("dns_server")
public class DnsService implements ServicesPlugin<DnsConfiguration> {
}

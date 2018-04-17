package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.docker.image.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

import static com.github.yassine.soxychains.subsystem.service.dns.DnsConfiguration.DNS_CONFIG_ID;

@RequiresImage(name = DNS_CONFIG_ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+DnsConfiguration.DNS_CONFIG_ID)
@ConfigKey(DNS_CONFIG_ID) @AutoService(ServicesPlugin.class)
public class DnsService implements ServicesPlugin<DnsConfiguration> {
}

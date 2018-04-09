package com.github.yassine.soxychains.subsystem.service.dns;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.RequiresImage;
import com.github.yassine.soxychains.subsystem.service.ServicesPlugin;
import com.google.auto.service.AutoService;

import static com.github.yassine.soxychains.subsystem.service.dns.DnsConfiguration.ID;

@RequiresImage(name = ID, resourceRoot = "classpath://com/github/yassine/soxychains/subsystem/service/"+DnsConfiguration.ID)
@AutoService(ServicesPlugin.class) @ConfigKey(ID)
public class DnsService implements ServicesPlugin<DnsConfiguration> {
}

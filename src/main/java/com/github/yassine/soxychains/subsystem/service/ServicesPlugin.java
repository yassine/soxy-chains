package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.Plugin;


/**
 * The main contract to fulfill for a given Service.
 * - Services typically run as container (not exclusively) on each host
 * - Services may declare dependencies through '@DependsOn' annotation from the 'guice-utils' module in order to require
 * another service to start before booting.
 * - Services requiring a custom docker image (one that needs to be build as part of the soxy-chains runtime) can use
 * the '@RequiresImage' annotation
 *
 * (WIP: Spec may evolve yet)
 *
 * @param <CONFIG>
 */
public interface ServicesPlugin<CONFIG extends ServicesPluginConfiguration> extends Plugin<CONFIG> {

}

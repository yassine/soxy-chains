/**
 * On each host, a couple of services are required to support the layer nodes by functionality such as Service discovery
 * (consul), proxy load-balancing (gobetween), or safe dns routing (to avoid dns leaks).
 * The Service logic has been designed with high-modularity in mind (even the configuration is pluggable!) so that future
 * functionality extension would be much more easier.
 */
package com.github.yassine.soxychains.subsystem.service;
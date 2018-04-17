# soxy-chains
![Build Status](https://www.travis-ci.org/yassine/soxy-chains.svg?branch=dev)
[![Coverage Status](https://coveralls.io/repos/github/yassine/soxy-chains/badge.svg?branch=dev)](https://coveralls.io/github/yassine/soxy-chains?branch=dev)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=com.github.yassine%3Asoxy-chains)](https://sonarcloud.io/dashboard/index/com.github.yassine:soxy-chains)
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?metric=sqale_rating&project=com.github.yassine%3Asoxy-chains)](https://sonarcloud.io/dashboard/index/com.github.yassine:soxy-chains)
[![Reliability](https://sonarcloud.io/api/project_badges/measure?metric=reliability_rating&project=com.github.yassine%3Asoxy-chains)](https://sonarcloud.io/dashboard/index/com.github.yassine:soxy-chains)

`soxy-chains` is a scalable multi-layer TCP traffic tunneling application, providing thus military-grade traffic 
encryption capabilities. It supports an arbitrary number of layers that spans an arbitrary number of hosts.

#### 1. Usage Scenario Examples
##### 1.1 Web crawling
###### 1.1.1 Scenario Requirements
1. A pool of 30 IP addresses for web crawling at max.
2. Avoid using Tor on the external layer as the addresses are more likely to be black-listed.
3. Only public VPN can be used, some of which may keep logs.
###### 1.1.2 Solution Topology
An satisfying solution consists of two layers of tunneling : VPN over Tor.
* We'll use a layer of tor nodes as entry points to keep us anonymous vis-à-vis the public VPN providers.
* We'll use an external layer of VPN nodes as a target-facing clients.
###### 1.1.3 Configuration
```
namespace: web_crawling_example
hosts:
  - address: localhost
    port: 2375
layers:
  - type: tor
    maxNodes: 5
    healthCheckInterval : 60s
    healthCheckTimeout  : 30s
    provider:
      type : local
      path : /some-path-to/onionoo-response.json
      minBandwidth : 8
      entryNodesCountries:
        - fr,de,gb,hu,it,pl,sl,se,dk,no,cz,bg,hr,ro,sc,nl,sk
      exitNodesCountries:
        - fr,de,gb,hu,it,pl,sl,se,dk,no,cz,bg,hr,ro,sc,nl,sk
  - type: vpn
    #Requirement 1
    maxNodes : 30
    healthCheckInterval : 60s
    healthCheckTimeout  : 30s
    provider :
      type: vpngate
      endpoint: http://130.158.75.33/api/iphone
      countries:
        - any
      excludeCountries:
        - none
```
#### 2. Scalability/Clustering
`soxy-chains` is docker-powered and can scale to an arbitrary number of hosts. You can use it on your local computer, over 
multiple virtual machines or via a cloud provider for larger setups.

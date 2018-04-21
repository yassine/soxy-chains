#!/bin/sh
docker events --filter "label=${NODE_LABEL}" --filter "label=${NAMESPACE_KEY}=${NAMESPACE}" --filter 'type=container' --filter 'event=start' --format '{{.ID}}' | while read container_id
do
  consul_host=${CONSUL_HOST}
  container_name=$(docker inspect --format='{{.Name}}' ${container_id})
  network_name=$(docker inspect --format="{{ .HostConfig.NetworkMode }}" ${container_id})
  ip_address=$(docker inspect --format="{{ (index (index .NetworkSettings.Networks ) \"$network_name\" ).IPAddress }}" ${container_id})
  port=1080
  service_key=$(docker inspect --format="{{ index .Config.Labels \"$SERVICE_KEY_LABEL\" }}" ${container_id})
  request="{
  \"ID\": \"$container_id\",
  \"Name\": \"$service_key\",
  \"Tags\": [
    \"$service_key\"
  ],
  \"Address\": \"$ip_address\",
  \"Port\": 1080,
  \"Meta\": {},
  \"EnableTagOverride\": false,
  \"Check\": {
    \"ID\":\"$container_id\",
    \"DockerContainerId\": \"$container_id\",
    \"Shell\": \"/bin/sh\",
    \"Args\": [\"/etc/scripts/health-check.sh\"],
    \"Interval\": \"60s\",
    \"Timeout\":\"10s\"
  }
}";
  `echo ${request} | curl -v -H "Content-Type: application/json" -X PUT -d @- http://${consul_host}:${CONSUL_PORT}/v1/agent/service/register`
done

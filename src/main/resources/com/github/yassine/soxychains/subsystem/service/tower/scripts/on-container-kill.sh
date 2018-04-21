#!/bin/sh
docker events --filter "label=${NODE_LABEL}" --filter "label=${NAMESPACE_KEY}=${NAMESPACE}" --filter 'type=container' --filter 'event=start' --format '{{.ID}}' | while read container_id
do
  consul_host=${CONSUL_HOST}
  `curl -H "Content-Type: application/json" -X PUT http://${consul_host}:${CONSUL_PORT}/v1/agent/service/deregister/${container_id}`
  `curl -H "Content-Type: application/json" -X PUT http://${consul_host}:${CONSUL_PORT}/v1/agent/check/deregister/${container_id}`
done

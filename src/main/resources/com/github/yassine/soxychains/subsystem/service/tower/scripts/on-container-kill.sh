#!/bin/sh
docker events --filter "label=${NODE_LABEL}" --filter "label=${NAMESPACE_KEY}=${NAMESPACE}" --filter 'type=container' --filter 'event=kill' --format='json' | while read event
do
  echo "detected node service kill : $event"
  container_id=`echo ${event} | jq '.id'`
  container_name=`docker inspect --format='{{.Name}}' ${container_id}`
  `curl -H "Content-Type: application/json" -X PUT http://${CONSUL_HOST}:${CONSUL_PORT}/agent/service/deregister/${container_name}`
done

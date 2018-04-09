#!/bin/sh
# arg: consul address, service name
result=$(curl -s http://$1:$2/v1/health/checks/$3?passing  | jq '.[].Status | select(contains("passing"))' |wc -l)
if [ ${result} -gt 0 ]; then
  exit 0
else
  exit 2
fi
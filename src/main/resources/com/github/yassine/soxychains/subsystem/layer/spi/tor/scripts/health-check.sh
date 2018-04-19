#!/bin/sh

curl --socks5 localhost:1080 https://check.torproject.org/api/ip  | jq '.IsTor' | grep true
exitCode=$?
if [ ! ${exitCode} -eq 0 ]
then
  exit 2
fi
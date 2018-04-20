#!/bin/sh

valid_ip()
{
  if [ "$(sipcalc $1 | grep ERR)" != "" ]; then
    return 0
  fi
}

ip link list | grep tun0
exitCode=$?
if [ ! ${exitCode} -eq 0 ]
then
  exit 2
fi
IP_ADDRESS=`curl -s --socks5 127.0.0.1:1080 https://api.ipify.org/?format=text`
IS_VALID_IP=$(valid_ip ${IP_ADDRESS})
if [ ${IS_VALID_IP} -eq 0 ]
then
  exit 2
else
  exit 0
fi
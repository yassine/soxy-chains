#!/bin/sh
#icanhazip.com / 64.182.208.182
valid_ip()
{
  if [ "$(sipcalc $1 | grep ERR)" != "" ]; then
    return 0
  fi
}

IP_ADDRESS=`curl -s --socks5 $1:$2 https://api.ipify.org/?format=text`

if [ "$IP_ADDRESS" = "" ]
then
  "Socks server is down"
  exit 2
fi

SELF_IP_ADDRESS=`curl -s https://myexternalip.com/raw`
IS_VALID_IP=$(valid_ip $IP_ADDRESS)

if [ "$SELF_IP_ADDRESS" = "$IP_ADDRESS" ]
then
  echo "self address '$SELF_IP_ADDRESS' is equal to ISP address '$IP_ADDRESS'"
  exit 2
elif [ $IS_VALID_IP ]
then
  echo "debug: invalid ip address $IP_ADDRESS"
  exit 2
else
  exit 0
fi

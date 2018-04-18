#!/bin/sh

CONFIG_FILE=/etc/config.ovpn
AUTH_FILE=/etc/vpn-auth.txt

if [ -z "${OVPN_USER}" ]
then
  echo "${OVPN_USER}" > ${AUTH_FILE}
fi

if [ -z "${OVPN_PASS}" ]
then
  echo "${OVPN_PASS}" >> ${AUTH_FILE}
fi

echo ${OVPN_CONFIG} | base64 -d | tee ${CONFIG_FILE}

if [ -f "${AUTH_FILE}" ]
then
  echo "auth-user-pass vpn-auth.txt" >> ${CONFIG_FILE}
fi
sockd -D -f /etc/config/dante/dante.conf
openvpn ${CONFIG_FILE}

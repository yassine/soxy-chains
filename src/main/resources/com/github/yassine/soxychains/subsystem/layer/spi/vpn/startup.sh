#!/bin/sh

CONFIG_FILE=/etc/config.ovpn
echo ${OVPN_CONFIG} > ${CONFIG_FILE}
openvpn ${CONFIG_FILE}

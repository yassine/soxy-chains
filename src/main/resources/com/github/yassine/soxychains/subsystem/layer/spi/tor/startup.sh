#!/bin/sh

TOR_CONFIG_FILE=/etc/tor-config

EXIT_NODES=${TOR_EXIT_NODES:=''}
ENTRY_NODES=${TOR_ENTRY_NODES:=''}
EXCLUDE_NODES=${TOR_EXCLUDE_NODES:=''}
EXCLUDE_EXIT_NODES=${TOR_EXCLUDE_EXIT_NODES:=''}

echo 'Log notice stdout' > ${TOR_CONFIG_FILE}
echo 'SocksPort 0.0.0.0:8080' >> ${TOR_CONFIG_FILE}
echo 'GeoIPExcludeUnknown 1' >> ${TOR_CONFIG_FILE}

if [ ! -z "${EXCLUDE_EXIT_NODES}" ]
then
  echo "ExcludeExitNodes ${EXCLUDE_EXIT_NODES}" >> ${TOR_CONFIG_FILE}
fi

if [ ! -z "${EXCLUDE_NODES}" ]
then
  echo "ExcludeNodes ${EXCLUDE_NODES}" >> ${TOR_CONFIG_FILE}
fi

if [ ! -z "${EXIT_NODES}" ]
then
  echo "ExitNodes ${EXIT_NODES}" >> ${TOR_CONFIG_FILE}
fi

if [ ! -z "${ENTRY_NODES}" ]
then
  echo "EntryNodes ${ENTRY_NODES}" >> ${TOR_CONFIG_FILE}
fi

tor -c ${TOR_CONFIG_FILE}

FROM alpine:edge

ADD config /etc/config
RUN apk update && \
    apk add --no-cache pdnsd && \
    rm -rf /var/cache/apk/* && \
    chmod 0444 /etc/config/pdnsd.conf && \
    mkdir -p /var/cache/pdnsd

ENTRYPOINT ["pdnsd"]
CMD ["--config-file=/etc/config/pdnsd.conf"]

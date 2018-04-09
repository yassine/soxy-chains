FROM alpine:edge

ADD config /etc/config/pdnsd
RUN echo http://dl-3.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/edge/main >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache pdnsd && \
    rm -rf /var/cache/apk/* && \
    chmod 0444 /etc/config/pdnsd/pdnsd.conf && \
    mkdir -p /var/cache/pdnsd

ENTRYPOINT ["pdnsd"]
CMD ["--config-file=/etc/pdnsd/config/pdnsd.conf"]

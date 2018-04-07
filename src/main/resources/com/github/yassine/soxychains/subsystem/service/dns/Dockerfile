FROM alpine:edge

RUN echo http://dl-3.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/edge/main >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache -U pdnsd supervisor && \
    rm -rf /var/cache/apk/*

ADD config /etc/config

RUN chmod 0444 /etc/config/pdnsd/pdnsd.conf \
    && mkdir -p /var/log/supervisor /var/cache/pdnsd

ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]
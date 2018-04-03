FROM alpine:latest

RUN echo http://dl-3.alpinelinux.org/alpine/latest-stable/community >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/latest-stable/testing >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/latest-stable/main >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache -U pdnsd supervisor && \
    rm -rf /var/cache/apk/*

ADD config /etc/config

RUN chmod 0444 /etc/config/pdnsd/pdnsd.conf

RUN mkdir -p /var/log/supervisor

ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]
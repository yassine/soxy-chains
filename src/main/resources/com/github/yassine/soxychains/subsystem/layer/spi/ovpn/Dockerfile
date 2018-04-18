FROM alpine:latest

ADD ./config /etc/config

RUN echo http://nl.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    echo http://nl.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache openvpn dante-server supervisor && \
    rm -rf /var/cache/apk/*
RUN mkdir -p /var/log/supervisor
RUN chmod +x /etc/config/openvpn/openvpn.sh
ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]

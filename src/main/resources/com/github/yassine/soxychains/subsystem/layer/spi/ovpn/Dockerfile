FROM alpine:latest

ADD ./config /etc/config
ADD ./scripts /etc/scripts

RUN echo http://nl.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    echo http://nl.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache openvpn dante-server supervisor sipcalc && \
    rm -rf /var/cache/apk/* && \
    mkdir -p /var/log/supervisor && \
    chmod +x /etc/config/openvpn/openvpn.sh && \
    chmod +x /etc/scripts/health-check.sh
ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]

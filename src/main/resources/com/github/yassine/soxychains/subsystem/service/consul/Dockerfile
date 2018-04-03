FROM alpine:latest

ENV CONSUL_VERSION  0.9.0
ENV CONSUL_PLATFORM amd64

ADD ./config /etc/config/
RUN echo http://dl-3.alpinelinux.org/alpine/latest-stable/community >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/latest-stable/testing >> /etc/apk/repositories && \
    apk update && \
    apk add --no-cache curl supervisor jq sipcalc && \
    curl -sSLo /tmp/consul.zip https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_${CONSUL_PLATFORM}.zip && \
    unzip -d /bin /tmp/consul.zip && \
    rm /tmp/consul.zip && \
    addgroup consul && \
    adduser -D -g "" -s /bin/sh -G consul consul && \
    rm -rf /etc/apk/cache && \
    mkdir -p /var/consul && \
    chown -R consul:consul /etc/config/consul && \
    chown -R consul:consul /var/consul && \
    mkdir -p /var/log/supervisor && \
    chmod +x /etc/config/consul/vpn-health-check.sh && \
    chmod +x /etc/config/consul/host-health-check.sh

ENTRYPOINT ["supervisord"]
CMD        ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]

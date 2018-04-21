FROM yassine/alpine-consul

ADD ./config /consul/config/

RUN echo http://dl-3.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    echo http://dl-3.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
    apk update && \
    apk upgrade && \
    apk add --no-cache curl jq && \
    chmod +x /consul/config/*.sh

ENTRYPOINT [ "consul" ]
CMD        [ "agent", "-dev", "-config-dir", "/consul/config" ]

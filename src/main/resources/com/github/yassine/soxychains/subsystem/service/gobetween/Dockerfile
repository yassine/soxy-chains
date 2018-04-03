FROM frolvlad/alpine-glibc

RUN apk upgrade --update

RUN apk add --no-cache curl supervisor && \
    curl -jksSL -o /tmp/gobetween.tar.gz https://github.com/yyyar/gobetween/releases/download/0.5.0/gobetween_0.5.0_linux_amd64.tar.gz && \
    gunzip /tmp/gobetween.tar.gz && \
    tar -C /etc -xf /tmp/gobetween.tar && \
    mv /etc/gobetween /etc/gobetween && \
    rm -rf /tmp/gobetween* && \
    ln -s /etc/gobetween /usr/bin/gobetween && \
    mkdir -p /data/config/gobetween && \
    apk del curl

ADD ./config /etc/config

RUN mkdir -p /var/log/supervisor

ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]
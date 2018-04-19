FROM alpine:edge

ENV DOCKER_VERSION 18.03.0
ENV PATH $PATH:/etc/scripts

ADD scripts /etc/scripts
ADD supervisord /etc/config/supervisord

RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq supervisor && \
    curl -L https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}-ce.tgz > /tmp/docker.tgz && \
    cd /tmp && tar xzf docker.tgz && mv /docker/docker /bin && \
    cd /tmp && rm -rf docker.tgz /tmp/docker && \
    chmod +x /etc/scripts/*.sh && \
    mkdir -p /var/log/supervisor && \
    rm -rf /var/cache/apk/*

ENTRYPOINT ["supervisord"]
CMD ["--nodaemon", "--configuration", "/etc/supervisord/config/supervisord.conf"]
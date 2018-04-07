FROM alpine:edge
MAINTAINER Yassine Echabbi <github.com/yassine>

ENV GOPATH /go-workspace
ENV CONSUL_VERSION v1.0.6
ENV PATH $GOPATH/bin:$PATH

ADD ./config /etc/config/
RUN apk update \
    && apk upgrade \
    # Permanent Deps
    && apk add --no-cache curl supervisor jq sipcalc \
    # Build Deps
    && apk add --no-cache --virtual .build-deps \
        make \
       	"go=1.10.1-r0" \
        git \
        gcc \
        libc-dev \
        libgcc \
        bash \
    && go get -u github.com/magiconair/vendorfmt/cmd/vendorfmt \
    && mkdir -p $GOPATH/src/github.com/hashicorp $GOPATH/bin \
    && cd $GOPATH/src/github.com/hashicorp \
    && git clone --branch $CONSUL_VERSION https://github.com/hashicorp/consul.git \
    && cd consul
    && make dev \
    && cp bin/consul /bin/consul \
    && addgroup consul \
    && adduser -D -g "" -s /bin/sh -G consul consul \
    && mkdir -p /var/consul /var/log/supervisor \
    && chown -R consul:consul /etc/config/consul /var/consul \
    && chmod +x /etc/config/consul/vpn-health-check.sh /etc/config/consul/host-health-check.sh\
    && apk del .build-deps \
    && rm -rf $GOPATH /var/cache/apk/*

ENTRYPOINT ["supervisord"]
CMD        ["--nodaemon", "--configuration", "/etc/config/supervisord/supervisord.conf"]

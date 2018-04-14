FROM alpine:edge

ADD startup.sh /bin/startup.sh
RUN apk update && \
    apk add --no-cache openvpn && \
    rm -rf /var/cache/apk/* && \
    chmod +x /bin/startup.sh

CMD ["startup.sh"]
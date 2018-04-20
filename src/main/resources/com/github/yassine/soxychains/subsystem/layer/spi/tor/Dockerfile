FROM alpine:edge

ADD startup.sh /bin/startup.sh
ADD ./scripts /etc/scripts

RUN apk update && \
    apk add --no-cache tor jq curl && \
    rm -rf /var/cache/apk/* && \
    chmod +x /bin/startup.sh && \
    chmod +x /etc/scripts/health-check.sh

CMD ["startup.sh"]
FROM yassine/alpine-consul

ADD ./config /consul/config/
RUN chmod +x /consul/config/*.sh

ENTRYPOINT [ "consul" ]
CMD        [ "agent", "-dev", "-config-dir", "/consul/config" ]

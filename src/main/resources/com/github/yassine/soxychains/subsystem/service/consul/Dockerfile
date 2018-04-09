FROM yassine/alpine-consul

ADD ./config /consul/config/

ENTRYPOINT [ "consul" ]
CMD        [ "agent", "-dev", "-config-dir", "/consul/config" ]

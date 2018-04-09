FROM yassine/alpine-gobetween

ADD ./config /etc/config/

ENTRYPOINT ["gobetween"]
CMD ["-c", "/etc/config/gobetween.toml"]
FROM yassine/alpine-gobetween:0.5.0

ADD ./config /etc/config/

ENTRYPOINT ["gobetween"]
CMD ["-c", "/etc/config/gobetween.toml"]
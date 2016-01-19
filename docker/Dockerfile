FROM ubuntu:14.04
RUN apt-get -y update && apt-get install -y jsvc supervisor
COPY greg.script /build/
COPY gReg /usr/local/gReg
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

EXPOSE 5001
EXPOSE 5002
CMD ["/usr/bin/supervisord"]

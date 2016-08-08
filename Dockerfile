FROM java:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /build

# Dependencies
ADD pom.xml /build/pom.xml
ADD node-1-config /build/node-1-config
ADD node-2-config /build/node-2-config
ADD docker-entrypoint.sh /
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Compile and package jar
ADD src /build/src
RUN ["mvn", "package"]

EXPOSE 5001
EXPOSE 5002

RUN ["ls", "/build/"]
CMD ["sh", "/docker-entrypoint.sh"]

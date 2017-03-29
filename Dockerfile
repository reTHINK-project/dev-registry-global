FROM openjdk:8
MAINTAINER snet.tu-berlin.de

# Install maven
#RUN apt-get update
#RUN apt-get install -y maven

WORKDIR /build

# Dependencies
#ADD pom.xml /build/pom.xml
#ADD *.cert.pem /build/
#ADD *.private.der /build/
#ADD rethink-ca /build/rethink-ca
#ADD docker-entrypoint.sh /
ADD target/ReThinkGlobalRegistry-0.3.3.jar app.jar
#RUN ["mvn", "dependency:resolve"]
#RUN ["mvn", "verify"]

# Compile and package jar
#ADD src /build/src
#RUN ["mvn", "package"]

EXPOSE 5001
EXPOSE 5002

#RUN ["ls", "/build/"]
#CMD ["sh", "/docker-entrypoint.sh"]
ENTRYPOINT ["java", "-jar", "app.jar"]
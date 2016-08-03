FROM java:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /build

# Dependencies
ADD pom.xml /build/pom.xml
ADD greg.config /build/greg.config
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Compile and package jar
ADD src /build/src
RUN ["mvn", "package"]

EXPOSE 5001
EXPOSE 5002
CMD ["java", "-jar", "/build/target/gReg/gReg.jar", "-c", "/build/"]

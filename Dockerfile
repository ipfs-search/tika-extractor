FROM maven:3-openjdk-11 AS build

# selectively add the POM file
COPY pom.xml /

# get all the downloads out of the way
RUN mvn verify clean --fail-never

# Build
COPY src /src
RUN mvn package -Dquarkus.package.type=uber-jar -DskipTests

FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=build /target/*-runner.jar /

ENV QUARKUS_HTTP_HOST=0.0.0.0

EXPOSE 8081
CMD ["sh", "-c", "java -jar *.jar"]

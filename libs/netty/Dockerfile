# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.8.1-openjdk-15-slim AS build
ENV APP_NAME=netty

COPY pom.xml /reddit/
WORKDIR /reddit

COPY libs libs
COPY apps apps

RUN mvn clean package -pl :${APP_NAME} -am -DskipTests

# Package stage
FROM openjdk:15-alpine 
ENV APP_NAME=netty

COPY --from=build /reddit/libs/${APP_NAME}/target/${APP_NAME}-1.0-SNAPSHOT.jar /usr/local/lib/${APP_NAME}.jar
EXPOSE 8080
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/usr/local/lib/netty.jar"]

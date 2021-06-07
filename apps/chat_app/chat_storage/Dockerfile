# Build stage
FROM maven:3.8.1-openjdk-15-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

# Package stage
FROM openjdk:15-alpine
ENV APP_NAME=chat_storage
COPY --from=build /app/target/${APP_NAME}-1.0-SNAPSHOT.jar /usr/local/lib/${APP_NAME}.jar
EXPOSE 15672
ENTRYPOINT ["java","-jar","/usr/local/lib/${APP_NAME}.jar"]

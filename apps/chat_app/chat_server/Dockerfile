# Build stage
FROM maven:3.8.1-openjdk-15-slim AS build
ENV APP_NAME=chat_app/chat_server

# Copy root XML
COPY pom.xml /reddit/

WORKDIR /reddit

# Copy libs
COPY libs libs

# Copy apps XML
COPY ./apps/pom.xml apps/

# Copy and build app
# COPY ./apps/${APP_NAME}/src apps/${APP_NAME}/
# COPY ./apps/${APP_NAME}/pom.xml apps/${APP_NAME}/
COPY apps apps

RUN mvn -f pom.xml -B package
# RUN mvn -f pom.xml clean package

# Package stage
FROM openjdk:15-Alpines 
ENV APP_NAME=chat_app/chat_server

COPY --from=build /reddit/apps/${APP_NAME}/target/${APP_NAME}-1.0-SNAPSHOT.jar /usr/local/lib/${APP_NAME}.jar
EXPOSE 5000

ENTRYPOINT ["java","-jar","/usr/local/lib/${APP_NAME}.jar"]

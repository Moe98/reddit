# Chat App

This is the chatting application service of Reddit.

## Installation

You have to use docker for running next commands.

To run Cassandra

```bash
docker run -d \ -p 9042:9042 \ --name cassandra \ cassandra:latest
```
To run Cassandra web (optional)
```bash
docker run -d \
-e CASSANDRA_HOST_IP=$(docker inspect --format '{{.NetworkSettings.IPAddress}}' cassandra) \
-e CASSANDRA_PORT=9042 \
-p 3000:3000 \
--name cassandra-web \
delermando/docker-cassandra-web:v0.4.0
```
Check `http://localhost:3000/` if you want to view the chat database through online UI.

To run RabbitMQ

```bash
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:management
```

## Running Apps

Now all you have to do is run the `ChatServer` class and `ChatStorageApp` class then enjoy using the chatting app.


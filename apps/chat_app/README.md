# Chat App

The chatting application of Reddit.

## Installing and Running Dependencies

You must have docker installed for running the next commands.

### Install and Run Cassandra

```bash
docker run -d \ -p 9042:9042 \ --name cassandra \ cassandra:latest
```

#### Install and Run Cassandra Web (optional)

If you want to view the chat database through online UI
```bash
docker run -d \
-e CASSANDRA_HOST_IP=$(docker inspect --format '{{.NetworkSettings.IPAddress}}' cassandra) \
-e CASSANDRA_PORT=9042 \
-p 3000:3000 \
--name cassandra-web \
delermando/docker-cassandra-web:v0.4.0
```
You can view the UI on `http://localhost:3000/`.

### Install and Run RabbitMQ

```bash
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:management
```

## Running Chat App

Now all you have to do is run the `ChatServer` class in `chat_server` module and `ChatStorageApp` class in `chat_storage` module, then enjoy using the chatting app.


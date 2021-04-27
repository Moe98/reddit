package org.sab.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer extends RPCBase {
    private static RPCServer instance = null;

    // creating a connection with RabbitMQ
    private RPCServer() throws IOException, TimeoutException {
        super();
    }

    // creating a singleton of the RPCServer
    public static RPCServer getInstance(String queueName) throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCServer();
        }

        instance.addChannel();
        instance.addQueue(queueName);
        return instance;
    }

    // TODO replace with method in super class
    private void addQueue(String queueName) throws IOException {
        // Initialize the queue which the RPCServer will be listening to.
        channel.queueDeclare(queueName, false, false, false, null);
    }

}

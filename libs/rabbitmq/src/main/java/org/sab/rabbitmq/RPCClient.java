package org.sab.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;

public class RPCClient implements AutoCloseable {

    private static RPCClient instance = null;
    private final Connection connection;

    // creating a connection with RabbitMQ
    private RPCClient() throws IOException, TimeoutException {
        super();
        connection = RPCBase.initConnection();
    }

    public static SingleClientChannel getSingleChannelExecutor() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCClient();
        }

        return new SingleClientChannel(instance.connection.createChannel());
    }

    // Close the |channel| as to not waste resources.
    public void close() throws IOException, TimeoutException {
        connection.close();
    }

}

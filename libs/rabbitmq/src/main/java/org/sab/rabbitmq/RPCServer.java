package org.sab.rabbitmq;

import com.rabbitmq.client.Connection;
import org.json.JSONObject;
import org.sab.functions.TriFunction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {
    private static RPCServer instance = null;
    private final Connection connection;

    // creating a connection with RabbitMQ
    private RPCServer() throws IOException, TimeoutException {
        super();
        connection = RPCBase.initConnection();
    }

    public static SingleServerChannel getSingleChannelExecutor(String queueName,
                                                               TriFunction<String, JSONObject, String> executor)
            throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCServer();
        }

        return new SingleServerChannel(instance.connection.createChannel(), queueName, executor);
    }

    // Close the |channel| as to not waste resources.
    public void close() throws IOException {
        connection.close();
    }

}

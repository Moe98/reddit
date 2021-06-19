package org.sab.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


class RPCBase {

    private static final String LOCALHOST  = "localhost";

    private RPCBase() {
    }

    public static Connection initConnection() throws TimeoutException, IOException {
        ConnectionFactory factory = new ConnectionFactory();

        String rabbitHost = System.getenv("RABBIT_HOST");
        if(rabbitHost == null)
            rabbitHost = LOCALHOST;

        factory.setHost(rabbitHost);

        return factory.newConnection();
    }

}

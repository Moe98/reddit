package org.sab.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


class RPCBase {


    private RPCBase() {
    }

    public static Connection initConnection() throws TimeoutException, IOException {
        ConnectionFactory factory = new ConnectionFactory();

        String rabbitHost = System.getenv("RABBIT_HOST");
        String rabbitUser = System.getenv("RABBIT_USER");
        String rabbitPassword = System.getenv("RABBIT_PASSWORD");

        if (rabbitHost != null)
            factory.setHost(rabbitHost);
        if (rabbitUser != null)
            factory.setUsername(rabbitUser);
        if (rabbitPassword != null)
            factory.setPassword(rabbitPassword);

        return factory.newConnection();
    }
}

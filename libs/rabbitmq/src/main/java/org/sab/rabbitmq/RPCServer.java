package org.sab.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DeliverCallback;
import org.sab.functions.TriFunction;

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

    // creating a monitor that always listens to the app specific queue
    public void listenOnQueue(String targetQueue, TriFunction<String, JSONObject, String> action) throws IOException {

        // TODO server will initialize the required queues from a config file

        // creating our monitor
        Object monitor = new Object();

        // creating a callback which would invoke the required command specified by the
        // JSON request message
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // adding the corrID of the response, which is the corrID of the request
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                    .correlationId(delivery.getProperties().getCorrelationId()).build();

            String response = "";
            try {
                // invoking command
                String req = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println("App: request: " + req);

                // TODO change command name
                JSONParser parser = new JSONParser();
                org.json.simple.JSONObject propertiesJson = (org.json.simple.JSONObject) parser.parse(req);
                Object commandName = propertiesJson.get("functionName");

                System.out.println("Prop JSON" + propertiesJson);
                System.out.println("-H- " + commandName);

                response += action.apply((String) commandName, new JSONObject(req));

                System.out.println("Sending: " + response);
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                // sending response message to the response queue, which is defined by the
                // request message's properties
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
                        response.getBytes(StandardCharsets.UTF_8));

                // sending a manual acknowledgement of sending the response
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                // RabbitMq consumer worker thread notifies the RPC server owner thread
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        // listening to the request queue, ready to consume a request and process it
        // using the above callback
        channel.basicConsume(targetQueue, false, deliverCallback, (consumerTag -> {
        }));

        // Wait and be prepared to consume the message from RPC client.
        while (true) {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

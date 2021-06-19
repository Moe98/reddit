package org.sab.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.functions.TriFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SingleServerChannel extends SingleBasicChannel {
    private final String queueName;
    private final TriFunction<String, JSONObject, String> executor;
    private String consumerTag;

    public SingleServerChannel(Channel channel, String queueName, TriFunction<String, JSONObject, String> executor) throws IOException {
        super(channel);
        this.queueName = queueName;
        this.executor = executor;
        declareSpecificQueue(queueName);
    }

    public void startListening() throws IOException {
        // listening to the request queue, ready to consume a request and process it
        // using the above callback
        consumerTag = channel.basicConsume(queueName, false, this::onDelivery, (consumerTag -> {
        }));
    }

    public void pauseListening() throws IOException {
        channel.basicCancel(consumerTag);
        // TODO, This is just a temporary solution
        // Waiting for some 50 milliseconds because basicCancel takes some time and it leads to problems when freezing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDelivery(String consumerTag, Delivery delivery) throws IOException {
        // creating a callback which would invoke the required command specified by the
        // JSON request message
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

            response += executor.apply((String) commandName, new JSONObject(req));

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

        }
    }
}

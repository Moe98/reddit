package org.sab.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SingleClientChannel extends SingleBasicChannel {

    public SingleClientChannel(Channel channel) {
        super(channel);
    }

    // Send |message| to |requestQueue| and receive the response from
    // |replyQueue|.
    public String call(String message, String requestQueue, String replyToQueue)
            throws IOException, InterruptedException {
        // Create a unique identifier for the request being placed in
        // |requestQueue|.
        final String corrId = UUID.randomUUID().toString();

        // Initialize the request queue.
        String declaredRequestQueue = declareSpecificQueue(requestQueue);

        // Initialize the response queue.
        String declaredReplyToQueue = declareSpecificQueue(replyToQueue);

        sendRequest(corrId, message, declaredRequestQueue, declaredReplyToQueue);

        return awaitResponse(corrId, declaredReplyToQueue);
    }

    // Send |message| to |requestQueue|
    public void call_withoutResponse(String message, String requestQueue)
            throws IOException {
        // Create a unique identifier for the request being placed in
        // |requestQueue|.
        final String corrId = UUID.randomUUID().toString();

        // Initialize the request queue.
        String declaredRequestQueue = declareSpecificQueue(requestQueue);

        sendRequest_withoutReplyTo(corrId, message, declaredRequestQueue);
    }

    public String awaitResponse(String corrId, String targetQueue) throws IOException, InterruptedException {
        // Create a blocking queue to put the expected response in.
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);


        final boolean isAutoAck = true;

        final DeliverCallback onReception = (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        };

        final CancelCallback onCancellation = consumerTag -> {};

        // Listen on the expected response queue, |targetQueue|, with the
        // callback function |onReception|, which checks that the response
        // queue received a response message with a correlation id of |corrId|.
        // If that is the case, put the response message in the blocking queue
        // response.
        final String channelTag = channel.basicConsume(targetQueue, isAutoAck, onReception, onCancellation);

        // Attempts to retrieve the response from the blocked queue, if the
        // response is not yet present, block the RPCClient until the callback
        // function, |onReception|, is able to retrieve the message.
        final String result = response.take();

        // Cancel any further consumptions from the channel.
        channel.basicCancel(channelTag);

        return result;
    }

    public void sendRequest(String corrId, String message, String targetQueue, String replyToQueue)
            throws IOException {

        final AMQP.BasicProperties senderProps = createSenderProps(corrId, replyToQueue);
        final String exchange = "";
        // sending the request message in the request queue with it's properties
        channel.basicPublish(exchange, targetQueue, senderProps, message.getBytes(StandardCharsets.UTF_8));
    }

    public void sendRequest_withoutReplyTo(String corrId, String message, String targetQueue)
            throws IOException {

        final AMQP.BasicProperties senderProps = createSenderProps(corrId, "");
        final String exchange = "";
        // sending the request message in the request queue with it's properties
        channel.basicPublish(exchange, targetQueue, senderProps, message.getBytes(StandardCharsets.UTF_8));
    }

    private static AMQP.BasicProperties createSenderProps(String corrId, String replyToQueue) {

        // adding the corrID of the request
        // adding the name of the response queue that the client server will listen for
        // a response on
        return new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyToQueue)
                .build();
    }

}

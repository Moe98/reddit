package org.sab.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.DeliverCallback;

public class RPCClient extends RPCBase implements AutoCloseable {

    private static RPCClient instance = null;

    // creating a connection with RabbitMQ
    private RPCClient() throws IOException, TimeoutException { 
        super();
    }

    public static RPCClient getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCClient();
        }

        instance.addChannel();
        return instance;
    }

    
    // Send |message| to |requestQueue| and receive the response from 
    // |replyQueue|.
    public String call(String message, String requestQueue, String replyToQueue)
            throws IOException, InterruptedException {
        // Create a unique identifier for the request being placed in 
        // |requestQueue|.
        final String corrId = UUID.randomUUID().toString();
        
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        final Map<String, Object> arguments = null;
        
        // Initialize the request queue.
        String declaredRequestQueue = declareQueue(requestQueue, durable, exclusive, autoDelete, arguments);
        // Initialize the response queue.
        String declaredReplyToQueue = declareQueue(replyToQueue, durable, exclusive, autoDelete, arguments);
        
        sendRequest(corrId, message, declaredRequestQueue, declaredReplyToQueue);

        return awaitResponse(corrId, declaredReplyToQueue);
    }
    
    private String awaitResponse(String corrId, String targetQueue) throws IOException, InterruptedException {
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

    // Close the |channel| as to not waste resources.
    public void close() throws IOException, TimeoutException {
        channel.close();
    }

}

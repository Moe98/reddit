package org.sab.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

abstract class SingleBasicChannel implements AutoCloseable {
    protected final Channel channel;

    public SingleBasicChannel(Channel channel) {
        this.channel = channel;
    }

    protected String declareSpecificQueue(String queue) throws IOException {
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        return channel.queueDeclare(queue, durable, exclusive, autoDelete, null).getQueue();
    }


    @Override
    public void close() throws IOException, TimeoutException {
        channel.close();
    }
}

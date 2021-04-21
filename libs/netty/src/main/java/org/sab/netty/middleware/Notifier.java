package org.sab.netty.middleware;

import com.rabbitmq.client.AMQP;
import org.json.JSONObject;
import org.sab.netty.Server;
import org.sab.rabbitmq.Receiver;

import java.util.concurrent.Callable;

public class Notifier implements Callable<String> {

    private JSONObject responseBody;
    private String queueName;
    private String correlationId;

    public Notifier(String queueName, String correlationId) {
        this.queueName = queueName;
        this.correlationId = correlationId;
    }

    @Override
    public String call() throws Exception {
        // TODO Add correlationId.

        Receiver consumer = new Receiver();
        JSONObject response = consumer.receive(queueName);
//        JSONObject response = consumer.receive(queueName, correlationId);
        String responseString = response.toString();
        setResponseBody(response);

        return responseString;
    }

    public JSONObject getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(JSONObject responseBody) {
        this.responseBody = responseBody;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }


}

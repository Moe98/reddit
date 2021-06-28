package org.sab.service.managers;

import org.json.JSONObject;
import org.sab.functions.TriFunction;
import org.sab.rabbitmq.RPCServer;
import org.sab.rabbitmq.SingleServerChannel;
import org.sab.service.ServiceConstants;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueManager {
    private SingleServerChannel singleServerChannel;

    private final InvocationManager invocationManager;
    private final String appUriName;

    public QueueManager(String appUriName, InvocationManager invocationManager) {
        this.appUriName = appUriName;
        this.invocationManager = invocationManager;
    }

    public void initAcceptingNewRequests() throws IOException, TimeoutException {
        initRPCServer();
    }
    
    public void initRPCServer() throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        final String queueName = appUriName.toUpperCase() + ServiceConstants.REQUEST_QUEUE_NAME_SUFFIX;

        TriFunction<String, JSONObject, String> invokeCallback = invocationManager::invokeCommand;
        singleServerChannel = RPCServer.getSingleChannelExecutor(queueName, invokeCallback);
    }

    public void stopAcceptingNewRequests() throws IOException {
        singleServerChannel.pauseListening();
    }

    public void startAcceptingNewRequests() throws IOException {
        singleServerChannel.startListening();
    }

    public void dispose() {
        if(singleServerChannel == null) {
            return;
        }
        try {
            stopAcceptingNewRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
        singleServerChannel = null;
    }
}

package org.sab.service;

import org.json.JSONObject;
import org.sab.rabbitmq.RPCServer;
import org.sab.functions.TriFunction;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {
    public static final String DEFAULT_PROPERTIES_FILENAME = "configmap.properties";
    private static final String REQUEST_QUEUE_NAME_SUFFIX = "_REQ"; 
    private ExecutorService threadPool;

    // Singleton Design Pattern
    public void getThreadPool(int threads) {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(threads);
        }
    }

    public abstract String getAppUriName();

    public abstract int getThreadCount();

    public abstract String getConfigMapPath();

    public void start() {
        final InputStream configMapStream = getClass().getClassLoader().getResourceAsStream(getConfigMapPath());
        try {
            ConfigMap.getInstance().instantiate(configMapStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getThreadPool(getThreadCount());

        try {
            listenOnQueue();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void listenOnQueue() throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        final String queueName = getAppUriName().toUpperCase() + REQUEST_QUEUE_NAME_SUFFIX;

        RPCServer server = RPCServer.getInstance(queueName);

        TriFunction<String, JSONObject, String> invokeCallback = (commandName, req) -> {
            try {
                return invokeCommand(commandName, req);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException
                    | ClassNotFoundException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        };
                
        // call the method in RPC server
        server.listenOnQueue(queueName, invokeCallback);

    }

    // function to invoke the required command using command pattern design
    public String invokeCommand(String commandName, JSONObject req) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ExecutionException, InterruptedException, ClassNotFoundException {
        // getting the class responsible for the command
        Class<?> commandClass;
        commandClass = ConfigMap.getInstance().getClass(commandName);
        if(commandClass == null)
            return "{\"statusCode\": 404, \"msg\": \"Incorrect Function-Name\"}";
        System.out.println("Command Class: " + commandClass);
        // creating an instance of the command class
        Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();

        // callback responsible for invoking the required method of the command class
        Callable<String> callable = () -> {
            try {
                return (String) commandClass.getMethod("execute", req.getClass()).invoke(commandInstance, req);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return "invoke command error";
        };

        // submitting the callback fn. to the thread pool
        Future<String> executorCallable = threadPool.submit(callable);

        // waiting until the future is done
        while (!executorCallable.isDone()) ;

        // returning the output of the future
        return executorCallable.get();
    }

}

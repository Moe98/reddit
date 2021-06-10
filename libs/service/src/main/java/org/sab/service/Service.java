package org.sab.service;

import org.json.JSONObject;
import org.sab.functions.TriFunction;
import org.sab.rabbitmq.RPCServer;
import org.sab.service.controllerbackdoor.Server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {
    public static final String DEFAULT_PROPERTIES_FILENAME = "configmap.properties";
    private static final String REQUEST_QUEUE_NAME_SUFFIX = "_REQ";
    private static final String THREADS_COUNT_PROPERTY_NAME = "threadsCount";
    private static int DEFAULT_THREADS_COUNT = 10;
    private ExecutorService threadPool;

    // Singleton Design Pattern
    private void getThreadPool(int threads) {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(threads);
        }
    }

    public abstract String getAppUriName();

    public int getThreadCount() {
        final Properties properties = new Properties();
        final InputStream threadsCountStream = getClass().getClassLoader().getResourceAsStream(THREADS_COUNT_PROPERTY_NAME.toLowerCase() + ".properties");

        if (threadsCountStream == null)
            return DEFAULT_THREADS_COUNT;
        else {
            try {
                properties.load(threadsCountStream);
            } catch (IOException e) {
                e.printStackTrace();
                return DEFAULT_THREADS_COUNT;
            }
            return Integer.parseInt(properties.getProperty(THREADS_COUNT_PROPERTY_NAME));
        }
    }

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
        listenToController();

    }

    private void freeze() {
        throw new UnsupportedOperationException();
    }

    private void resume() {
        throw new UnsupportedOperationException();
    }

    private void reloadThreadPool() {
        throw new UnsupportedOperationException();
    }

    private void reloadCommandMap() {
        throw new UnsupportedOperationException();
    }

    private void reloadDbPool() {
        throw new UnsupportedOperationException();
    }

    public void listenOnQueue() throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        final String queueName = getAppUriName().toUpperCase() + REQUEST_QUEUE_NAME_SUFFIX;

        RPCServer server = RPCServer.getInstance(queueName);

        TriFunction<String, JSONObject, String> invokeCallback = this::invokeCommand;

        // call the method in RPC server
        server.listenOnQueue(queueName, invokeCallback);

    }

    private void listenToController() {
        new Thread(() -> {
            try {
                new Server(8080, this).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Object is a placeholder.
    private void receiveFile(Object file) {
        throw new UnsupportedOperationException();
    }

    private void reloadClass(String className) {
        throw new UnsupportedOperationException();
    }

    // function to invoke the required command using command pattern design
    public String invokeCommand(String commandName, JSONObject req) {
        final Callable<String> callable = () -> runCommand(commandName, req);

        // submitting the callback fn. to the thread pool
        Future<String> executorCallable = threadPool.submit(callable);

        // waiting until the future is done
        while (!executorCallable.isDone()) ;

        // returning the output of the future
        try {
            return executorCallable.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{\"statusCode\": 507, \"msg\": \"Execution was interrupted\"}";
        } catch (ExecutionException e) {
            return "{\"statusCode\": 521, \"msg\": \"Execution threw an exception\"}";
        }
    }

    private String runCommand(String commandName, JSONObject req) {
        try {
            // getting the class responsible for the command
            final Class<?> commandClass = ConfigMap.getInstance().getClass(commandName);
            // creating an instance of the command class
            final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
            // callback responsible for invoking the required method of the command class
            return (String) commandClass.getMethod("execute", req.getClass()).invoke(commandInstance, req);
        } catch (ClassNotFoundException e) {
            return "{\"statusCode\": 404, \"msg\": \"Function-Name class: (" + commandName + ") not found\"}";
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            return "{\"statusCode\": 504, \"msg\": \"Function-Name class: not operational\"}";
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return "{\"statusCode\": 510, \"msg\": \"Function-Name class: threw an exception\"}";
        }
    }

}

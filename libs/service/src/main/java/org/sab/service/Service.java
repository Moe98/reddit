package org.sab.service;

import org.json.JSONObject;
import org.sab.controller.Controller;
import org.sab.functions.TriFunction;
import org.sab.io.IoUtils;
import org.sab.rabbitmq.RPCServer;
import org.sab.reflection.ReflectionUtils;
import org.sab.service.controllerbackdoor.BackdoorServer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {
    public static final String DEFAULT_PROPERTIES_FILENAME = "commandmap.properties";
    private static final String REQUEST_QUEUE_NAME_SUFFIX = "_REQ";
    private static final int MAX_THREAD_TIMEOUT = 4;
    private static final String THREADS_COUNT_PROPERTY_NAME = "threadsCount", DB_CONNECTIONS_COUNT_PROPERTY_NAME = "dbConnectionsCount";
    private static final int DEFAULT_THREADS_COUNT = 10, DEFAULT_DB_CONNECTIONS_COUNT = 10;
    private final Properties configProperties = new Properties();
    private ExecutorService threadPool;
    private RPCServer messagingServer;

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(getThreadCount());
    }

    public abstract String getAppUriName();

    public int readProperty(String propertyName, int defaultPropertyValue) {
        if (!configProperties.containsKey(propertyName))
            return defaultPropertyValue;
        return Integer.parseInt(configProperties.getProperty(propertyName));
    }

    private final int getThreadCount() {
        return readProperty(THREADS_COUNT_PROPERTY_NAME, DEFAULT_THREADS_COUNT);
    }

    public final int getDbConnectionsCount() {
        return readProperty(DB_CONNECTIONS_COUNT_PROPERTY_NAME, DEFAULT_DB_CONNECTIONS_COUNT);
    }

    public abstract String getConfigMapPath();

    private void loadCommandMap() {
        final InputStream configMapStream = getClass().getClassLoader().getResourceAsStream(getConfigMapPath());
        try {
            ConfigMap.getInstance().instantiate(configMapStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        listenToController();
        loadProperties();
        initAcceptingNewRequests();
        resume();
    }

    private void loadProperties() {
        loadCommandMap();
        loadConfigProperties();
    }

    private void loadConfigProperties() {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            configProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void freeze() {
        stopAcceptingNewRequests();
        releaseThreadPool();
        releaseDbPool();
    }

    public void resume() {
        initThreadPool();
        startAcceptingNewRequests();
    }

    private void reloadThreadPool() {
        stopAcceptingNewRequests();
        releaseThreadPool();
        initThreadPool();
        startAcceptingNewRequests();
    }


    private void reloadDbPool() {
        throw new UnsupportedOperationException();
    }

    private void initAcceptingNewRequests() {
        try {
            initRPCServer();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void stopAcceptingNewRequests() {
        try {
            messagingServer.pauseListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAcceptingNewRequests() {
        try {
            messagingServer.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseThreadPool() {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(MAX_THREAD_TIMEOUT, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    private void releaseDbPool() {
        // TODO
    }

    public void initRPCServer() throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        final String queueName = getAppUriName().toUpperCase() + REQUEST_QUEUE_NAME_SUFFIX;

        TriFunction<String, JSONObject, String> invokeCallback = this::invokeCommand;
        messagingServer = RPCServer.getInstance(queueName, invokeCallback);
    }

    private void listenToController() {
        new Thread(() -> {
            try {
                new BackdoorServer(getControllerPort(), this).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private InputStream decodedFileToInputStream(String encodedFile) {
        return IoUtils.decodeFile(encodedFile);
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


    public int getControllerPort() {
        final InputStream stream = Controller.class.getClassLoader().getResourceAsStream("apps-ports.properties");
        final Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String propertyName = getAppUriName().toLowerCase();
        return Integer.parseInt(properties.getProperty(propertyName));

    }

    public void handleControllerMessage(JSONObject message) {
        System.out.printf("%s has received a message from the controller!\n%s\n", getAppUriName(), message.toString());
        Method method = ReflectionUtils.getMethod(Service.class, message.getString("command"));
        try {
            boolean hasArgs = message.has(Controller.ARGS);
            method.invoke(this, hasArgs ? message.optJSONArray(Controller.ARGS).toList().toArray() : null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setMaxThreadsCount(int maxThreadsCount) {
        updateProperty(THREADS_COUNT_PROPERTY_NAME, String.valueOf(maxThreadsCount));
        reloadThreadPool();

    }

    public void setMaxDbConnectionsCount(int maxDbConnectionsCount) {
        updateProperty(DB_CONNECTIONS_COUNT_PROPERTY_NAME, String.valueOf(maxDbConnectionsCount));
        reloadDbPool();
    }

    private void updateProperty(String propertyName, String newValue) {
        configProperties.replace(propertyName, newValue);
    }

    public void addCommand(String commandName, String encodedFile) {
        ConfigMap.getInstance().addCommand(commandName);
        // TODO ADD to JVM
        throw new UnsupportedOperationException();
    }

    public void deleteCommand(String functionName) {
        ConfigMap.getInstance().deleteCommand(functionName);
    }

    public void updateCommand(String commandName, String encodedFile) {
        // TODO
        throw new UnsupportedOperationException();
    }

}

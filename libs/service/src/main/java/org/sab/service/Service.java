package org.sab.service;

import org.json.JSONObject;
import org.sab.controller.Controller;
import org.sab.databases.PoolDoesNotExistException;
import org.sab.databases.PooledDatabaseClient;
import org.sab.functions.TriFunction;
import org.sab.io.IoUtils;
import org.sab.rabbitmq.RPCServer;
import org.sab.rabbitmq.SingleServerChannel;
import org.sab.reflection.ReflectionUtils;
import org.sab.service.controllerbackdoor.BackdoorServer;
import org.sab.service.databases.DBConfig;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {
    private final Properties configProperties = new Properties();
    protected final HashMap<String, DBConfig> requiredDbs = new HashMap<>();

    private boolean isFrozen = true;
    private ExecutorService threadPool;
    private SingleServerChannel singleServerChannel;

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(getThreadCount());
    }

    private void initDbPool() {

        for(DBConfig dbConfig : requiredDbs.values()) {
            final Class<?> clazz;
            PooledDatabaseClient clientInstance = null;
            String dbName = dbConfig.getName();
            try {
                clazz = ConfigMap.getInstance().getDb(dbName);
                System.out.println("DB class: " + clazz);

                clientInstance = (PooledDatabaseClient) clazz.getMethod(ServiceConstants.GET_DB_CLIENT_METHOD_NAME).invoke(null);
                System.out.println("Client Instance: " + clientInstance);

            } catch (ClassNotFoundException e) {
                System.err.println("Database class: (" + dbName + ") not found");
                e.printStackTrace();
                shutdownGracefully();
            } catch (NoSuchMethodException e) {
                System.err.println("Compulsory method in " + dbName + " class (" + ServiceConstants.GET_DB_CLIENT_METHOD_NAME + ") not found");
                e.printStackTrace();
                shutdownGracefully();
            } catch (IllegalAccessException e) {
                System.err.println("Db class: not operational");
                e.printStackTrace();
                shutdownGracefully();
            } catch (InvocationTargetException e) {
                System.err.println("DB class: threw an exception");
                e.printStackTrace();
                shutdownGracefully();
            }

            int connectionCount = dbConfig.getConnectionCount();
            clientInstance.createPool(connectionCount);

            dbConfig.setClient(clientInstance);
            System.out.println("Initialized the " + dbConfig.getName() + " pool");
        }
    }


    public abstract String getAppUriName();


    public int readIntProperty(String propertyName, int defaultPropertyValue) {
        if (!configProperties.containsKey(propertyName))
            return defaultPropertyValue;
        return Integer.parseInt(configProperties.getProperty(propertyName));
    }

    public ArrayList<String> readArrayProperty(String propertyName, String delimiter, ArrayList<String> defaultPropertyValue) {
        if (!configProperties.containsKey(propertyName))
            return defaultPropertyValue;
        return new ArrayList<String>(Arrays.asList(configProperties.getProperty(propertyName).split(delimiter)));
    }

    private int getThreadCount() {
        return readIntProperty(ServiceConstants.THREADS_COUNT_PROPERTY_NAME, ServiceConstants.DEFAULT_THREADS_COUNT);
    }

    public final String getConfigMapPath() {
        return ServiceConstants.DEFAULT_PROPERTIES_FILENAME;
    }

    public final String getDbMapPath() {
        return ServiceConstants.DEFAULT_DB_PROPERTIES_FILENAME;
    }

    private void loadCommandMap() {
        final InputStream configMapStream = getClass().getClassLoader().getResourceAsStream(getConfigMapPath());

        try {
            ConfigMap.getInstance().instantiateCmdMap(configMapStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDbMap() {
        final InputStream dbMapStream = Service.class.getClassLoader().getResourceAsStream(getDbMapPath());
        try {
            ConfigMap.getInstance().instantiateDbMap(dbMapStream);
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

    protected void shutdownGracefully() {
        // TODO release resources and halt app
        System.exit(-1);
    }

    private void loadProperties() {
        loadCommandMap();
        loadDbMap();
        loadConfigProperties();
        parseDbProperties();
    }

    private void loadConfigProperties() {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            configProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            shutdownGracefully();
        }
    }

    private void parseDbProperties() {
        ArrayList<String> requiredDBsList = readArrayProperty(ServiceConstants.REQUIRED_DATABASES_PROPERTY_NAME,
                    ServiceConstants.REQUIRED_DATABASES_ARRAY_DELIMITER,
                    ServiceConstants.DEFAULT_REQUIRED_DATABASES);

        for(String dbPair : requiredDBsList) {
            String [] split = dbPair.split(ServiceConstants.REQUIRED_DATABASES_PAIR_DELIMITER);
            String dbName = split[0];
            int connectionCount = Integer.parseInt(split[1]);
            this.requiredDbs.put(dbName, new DBConfig(dbName, connectionCount));
        }

        System.out.println("Required Dbs: "  + this.requiredDbs);
    }

    public void freeze() {
        if (isFrozen) {
            return;
        }

        stopAcceptingNewRequests();
        releaseThreadPool();
        releaseDbPools();

        isFrozen = true;
    }

    public void resume() {
        if (!isFrozen) {
            return;
        }

        initThreadPool();
        initDbPool();
        startAcceptingNewRequests();

        isFrozen = false;
    }

    private void reloadThreadPool() {
        stopAcceptingNewRequests();
        releaseThreadPool();
        initThreadPool();
        startAcceptingNewRequests();
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
            singleServerChannel.pauseListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAcceptingNewRequests() {
        try {
            singleServerChannel.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseThreadPool() {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(ServiceConstants.MAX_THREAD_TIMEOUT, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    private void releaseDbPools() {
        for(DBConfig dbConfig : requiredDbs.values()) {
            try {
                dbConfig.getClient().destroyPool();
            } catch (PoolDoesNotExistException e) {
                e.printStackTrace();
            }
        }
    }

    public void initRPCServer() throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        final String queueName = getAppUriName().toUpperCase() + ServiceConstants.REQUEST_QUEUE_NAME_SUFFIX;

        TriFunction<String, JSONObject, String> invokeCallback = this::invokeCommand;
        singleServerChannel = RPCServer.getSingleChannelExecutor(queueName, invokeCallback);
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
        updateProperty(ServiceConstants.THREADS_COUNT_PROPERTY_NAME, String.valueOf(maxThreadsCount));
        reloadThreadPool();

    }

    public void setMaxDbConnectionsCount(String dbName, int maxDbConnectionsCount) {
        DBConfig dbToModify = requiredDbs.get(dbName);
        dbToModify.setConnectionCount(maxDbConnectionsCount);

        String dbs = requiredDbs.values()
                .stream()
                .map(dbConfig -> dbConfig.getName() + ServiceConstants.REQUIRED_DATABASES_PAIR_DELIMITER + dbConfig.getConnectionCount())
                .collect(Collectors.joining(ServiceConstants.REQUIRED_DATABASES_ARRAY_DELIMITER));
        updateProperty(ServiceConstants.REQUIRED_DATABASES_PROPERTY_NAME, dbs);

        try {
            dbToModify.getClient().setMaxConnections(maxDbConnectionsCount);
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }

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

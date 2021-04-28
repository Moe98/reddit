package org.sab.service;

import org.json.JSONObject;
import org.sab.rabbitmq.RPCServer;
import org.sab.rabbitmq.TriFunction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {
    private static ExecutorService threadPool;

    // Singleton Design Pattern
    public static void getThreadPool(int threads) {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(threads);
        }
    }

    public static void listenOnQueue(String queueName) throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which
        // the app listens
        RPCServer server = RPCServer.getInstance(queueName);

        TriFunction<String, JSONObject, String> invokeCallback = (commandName, req) -> {
            try {
                return invokeCommand(commandName, req);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException
                    | ClassNotFoundException | ExecutionException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        };
                
        // call the method in RPC server
        server.listenOnQueue(queueName, invokeCallback);

    }

    // function to invoke the required command using command pattern design
    public static String invokeCommand(String commandName, JSONObject req) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ExecutionException, InterruptedException, ClassNotFoundException {
        // getting the class responsible for the command
        Class<?> commandClass = ConfigMap.getClass(commandName);
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

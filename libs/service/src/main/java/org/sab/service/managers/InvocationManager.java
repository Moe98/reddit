package org.sab.service.managers;

import org.json.JSONObject;
import org.sab.service.Command;
import org.sab.service.ConfigMap;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InvocationManager {

    private final ThreadPoolManager threadPoolManager;
    private final ClassManager classManager;

    public InvocationManager(ThreadPoolManager threadPoolManager, ClassManager classManager) {
        this.threadPoolManager = threadPoolManager;
        this.classManager = classManager;
    }

    // function to invoke the required command using command pattern design
    public String invokeCommand(String commandName, JSONObject req) {
        final Callable<String> callable = () -> runCommand(commandName, req);

        // submitting the callback fn. to the thread pool
        Future<String> executorCallable = threadPoolManager.submit(callable);

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

    private String runCommand(String functionName, JSONObject req) {
        try {
            // getting the class responsible for the command
            final Class<?> commandClass = classManager.getCommand(functionName);
            // creating an instance of the command class
            final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
            // callback responsible for invoking the required method of the command class
            return (String) commandClass.getMethod("execute", req.getClass()).invoke(commandInstance, req);
        } catch (ClassNotFoundException e) {
            return "{\"statusCode\": 404, \"msg\": \"Function-Name class: (" + functionName + ") not found\"}";
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            return "{\"statusCode\": 504, \"msg\": \"Function-Name class: not operational\"}";
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return "{\"statusCode\": 510, \"msg\": \"Function-Name class: threw an exception\"}";
        }
    }

}

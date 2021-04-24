package org.sab.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.rabbitmq.RPCServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
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

    // creating a monitor that always listens to the app specific queue
    public static void listenOnQueue(String queueName) throws IOException, TimeoutException {
        // initializing a connection with rabbitMQ and initializing the queue on which the app listens
        RPCServer server = RPCServer.getInstance(queueName);
        Channel channel = server.getChannel();
        // TODO server will initialize the required queues from a config file

        // creating our monitor
        Object monitor = new Object();

        // creating a callback which would invoke the required command specified by the JSON request message
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // adding the corrID of the response, which is the corrID of the request
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                // invoking command
                String req = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println("App: request: " + req);

                // TODO change command name
                JSONParser parser = new JSONParser();
                org.json.simple.JSONObject propertiesJson = (org.json.simple.JSONObject) parser.parse(req);
                Object commandName = propertiesJson.get("functionName");

                System.out.println("Prop JSON" + propertiesJson);
                System.out.println("-H- " + commandName);

                response += invokeCommand((String) commandName, new JSONObject(req));

                System.out.println("Sending: " + response);

            } catch (RuntimeException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ExecutionException | InterruptedException | ClassNotFoundException | ParseException e) {
                System.out.println(" [.] " + e.toString());
            } finally {
                // sending response message to the response queue, which is defined by the request message's properties
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));


                // sending a manual acknowledgement of sending the response
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                // RabbitMq consumer worker thread notifies the RPC server owner thread
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };

        // listening to the request queue, ready to consume a request and process it using the above callback
        channel.basicConsume(queueName, false, deliverCallback, (consumerTag -> {
        }));

        // Wait and be prepared to consume the message from RPC client.
        while (true) {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
                // TODO 1st option
                //  Do we need to invoke call here?
//                    commandClass.getMethod("setRequest",  req.getClass()).invoke(commandInstance, req);
//                    FutureTask f1 = (FutureTask) executor.submit(callable);
//                    String res = (String) commandClass.getMethod("call").invoke(commandInstance);
//                    System.out.println("Call returned: " + res);
//                    return res;
                // TODO 2nd option (invoking execute directly)
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

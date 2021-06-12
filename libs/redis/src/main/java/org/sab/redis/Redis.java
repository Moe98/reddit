package org.sab.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.naming.TimeLimitExceededException;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class Redis {

    private StatefulRedisConnection<String, String> connection;
    private RedisClient redisClient;
    private RedisAsyncCommands<String, String> asyncCommands;

    // TODO get from config file
    // Very top secret password.
    private final String HOST_URI = "127.0.0.1";
    private final int port = 6379;
    private final String PASSWORD = "mypass";
    private final int OPERATION_TIMEOUT_MINUTES = 1;
    private final int CONNECTION_TIMEOUT_SECONDS = 60;
    private final int DATABASE_NUMBER = 0;

    final static String TIMEOUT_ERROR_MESSAGE = "Could not complete within the timeout";

    // TODO singleton?
    //  check if we can pool the connection
    public Redis() {
        /*
        connection scheme
            redis :// [password@] host [: port] [/ database]
                [? [timeout=timeout[d|h|m|s|ms|us|ns]]
                [&_database=database_]]
        */

        RedisURI uri = RedisURI.Builder.redis(HOST_URI, port)
                        .withPassword(PASSWORD)
                        .withDatabase(DATABASE_NUMBER)
                        .build();
        
        redisClient = RedisClient.create(uri);
        // TODO what happens after the connection times out?
        redisClient.setDefaultTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));
        
        connection = redisClient.connect();
    }

    public void connect() {
        connection = redisClient.connect();
    }

    public StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }

    // TODO should we also have a sync option??
    public void setCommand() {
        asyncCommands = connection.async();
    }
    
    public String setKeyVal(String key, String value) throws TimeLimitExceededException {

        RedisFuture<String> future = asyncCommands.set(key, value);

        String status = "";
        try{
            status = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e){
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return status;
    }

    public String getKeyVal(String key) throws TimeLimitExceededException {
        
        String value;

        try {
            RedisFuture<String> future = asyncCommands.get(key);
            value = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return value;
    }

    public long deleteKey(String... keys) throws TimeLimitExceededException {

        RedisFuture<Long> future = asyncCommands.del(keys);
        long num;
        
        try{
            num = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e){
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return num;
    }
    
    public long existsKey(String... keys) throws TimeLimitExceededException {

        long num;
        RedisFuture<Long> future = asyncCommands.exists(keys);
        
        try{
            num = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e){
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return num;
    }

    public boolean expireKey(String key, long seconds) throws TimeLimitExceededException {

        boolean willExpire;

        try{
            RedisFuture<Boolean> future = asyncCommands.expire(key, seconds);   
            willExpire = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return willExpire;
    }

    public void setAllKeyVal(ArrayList<String> keys, ArrayList<String> values) throws TimeLimitExceededException {
        // TODO length of 2 lists
        
        List<RedisFuture<String>> futures = new ArrayList<>();

        int i = 0;
        for (String key : keys) {
            futures.add(asyncCommands.set(key, values.get(i)));
            i++;
        }   

        try {
            LettuceFutures.awaitAll(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }
        

    }

    public long setArr(String arrName, String... values) throws TimeLimitExceededException {
        long newLen;

        RedisFuture<Long> future = asyncCommands.rpush(arrName, values);
        try{
            newLen = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return newLen;
    }

    public long appendToArr(String arrName, String... items) throws TimeLimitExceededException {
        long newLen;

        try{
            RedisFuture<Long> future = asyncCommands.rpushx(arrName, items);
            newLen = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return newLen;
    }
    
    public long getArrLength(String arrName) throws TimeLimitExceededException {

        long len;
        
        try{
            RedisFuture<Long> future = asyncCommands.llen(arrName);
            len = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return len;
    }

    public List<String> getArrRange(String arrName, int start, int stop) throws TimeLimitExceededException {

        List<String> arr;
        
        try{ 
            RedisFuture<List<String>> future = asyncCommands.lrange(arrName, start, stop);
            arr = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        return arr;
    }


    public void closeConnection() {
        connection.close();
    }

    public void shutdown() {
        redisClient.shutdown();
    }

}

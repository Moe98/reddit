package org.sab.redis;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.naming.TimeLimitExceededException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static io.lettuce.core.LettuceFutures.awaitAll;
import static io.lettuce.core.cluster.ClusterTopologyRefreshOptions.DEFAULT_REFRESH_PERIOD_DURATION;

public class Redis {

    final static String TIMEOUT_ERROR_MESSAGE = "Could not complete within the timeout";
    private final RedisClusterClient redisClient;
    // TODO get from config file
//     private final String HOST_URI = System.getenv("REDIS_HOST_URI");
    private final String HOST_URI = "127.0.0.1";
    private final ArrayList<Integer> ports = new ArrayList<>(Arrays.asList(7000, 7001, 7002, 7003, 7004, 7005));
    // Very top secret password.
    private final String PASSWORD = System.getenv("REDIS_PASSWORD");
    private final int OPERATION_TIMEOUT_MINUTES;
    private final int DATABASE_NUMBER;
    private final int NUMBER_OF_CONNECTIONS;
    private GenericObjectPool<StatefulRedisClusterConnection<String, String>> connectionPool;

    // TODO singleton?
    //  check if we can pool the connection
    public Redis() {
        /*
        connection scheme
            redis :// [password@] host [: port] [/ database]
                [? [timeout=timeout[d|h|m|s|ms|us|ns]]
                [&_database=database_]]
        */

        final Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        OPERATION_TIMEOUT_MINUTES = Integer.parseInt(properties.getProperty("OPERATION_TIMEOUT_MINUTES"));
        DATABASE_NUMBER = Integer.parseInt(properties.getProperty("DATABASE_NUMBER"));
        NUMBER_OF_CONNECTIONS = Integer.parseInt(properties.getProperty("NUMBER_OF_CONNECTIONS"));
//         OPERATION_TIMEOUT_MINUTES = 1;
//         DATABASE_NUMBER = 0;
//         NUMBER_OF_CONNECTIONS = 10;

        ArrayList<RedisURI> redisURIs = new ArrayList<>();
        for (int port : this.ports) {
            RedisURI uri = RedisURI.Builder.redis(HOST_URI, port)
                    .withPassword(PASSWORD)
                    .withDatabase(DATABASE_NUMBER)
                    .build();
            redisURIs.add(uri);
        }

        redisClient = RedisClusterClient.create(redisURIs);

        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(DEFAULT_REFRESH_PERIOD_DURATION)
                .build();

        redisClient.setOptions(ClusterClientOptions.builder()
                .autoReconnect(true)
                .cancelCommandsOnReconnectFailure(true)
                .disconnectedBehavior(ClusterClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .topologyRefreshOptions(topologyRefreshOptions)
                .build());

        connect();
    }

    public void connect() {
        connectionPool = ConnectionPoolSupport.createGenericObjectPool(
                redisClient::connect, new GenericObjectPoolConfig<>());
        connectionPool.setMaxTotal(NUMBER_OF_CONNECTIONS);
    }

    public StatefulRedisClusterConnection<String, String> getConnection() throws Exception {
        return connectionPool.borrowObject();
    }

    public RedisAdvancedClusterAsyncCommands<String, String> getAsyncCommand(StatefulRedisClusterConnection<String, String> connection) {
        return connection.async();
    }

    public RedisAdvancedClusterCommands<String, String> getSyncCommand(StatefulRedisClusterConnection<String, String> connection) {
        return connection.sync();
    }

    public String setKeyVal(String key, String value) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<String> future = asyncCommands.set(key, value);
        String status;

        try {
            status = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return status;
    }

    public String getKeyVal(String key) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<String> future = asyncCommands.get(key);
        String value;

        try {
            value = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return value;
    }

    public long deleteKey(String... keys) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Long> future = asyncCommands.del(keys);
        long num;

        try {
            num = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return num;
    }

    public long existsKey(String... keys) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Long> future = asyncCommands.exists(keys);
        long num;

        try {
            num = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return num;
    }

    public boolean expireKey(String key, long seconds) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Boolean> future = asyncCommands.expire(key, seconds);
        boolean willExpire;

        try {
            willExpire = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return willExpire;
    }

    public void setAllKeyVal(ArrayList<String> keys, ArrayList<String> values) throws Exception {

        // TODO length of 2 lists
        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        List<RedisFuture<String>> futures = new ArrayList<>();

        int i = 0;
        for (String key : keys) {
            futures.add(asyncCommands.set(key, values.get(i)));
            i++;
        }

        try {
            awaitAll(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

    }

    public long setArr(String arrName, String... values) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Long> future = asyncCommands.rpush(arrName, values);
        long newLen;

        try {
            newLen = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return newLen;
    }

    public long appendToArr(String arrName, String... items) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Long> future = asyncCommands.rpushx(arrName, items);
        long newLen;

        try {
            newLen = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return newLen;
    }

    public long getArrLength(String arrName) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<Long> future = asyncCommands.llen(arrName);
        long len;

        try {
            len = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return len;
    }

    public List<String> getArrRange(String arrName, int start, int stop) throws Exception {

        StatefulRedisClusterConnection<String, String> connection = getConnection();
        RedisAdvancedClusterAsyncCommands<String, String> asyncCommands = getAsyncCommand(connection);

        RedisFuture<List<String>> future = asyncCommands.lrange(arrName, start, stop);
        List<String> arr;

        try {
            arr = future.get(OPERATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new TimeLimitExceededException(TIMEOUT_ERROR_MESSAGE);
        }

        returnConnection(connection);

        return arr;
    }

    public void closeConnection() {
        connectionPool.close();
    }

    public void returnConnection(StatefulRedisClusterConnection<String, String> connection) {
        this.connectionPool.returnObject(connection);
    }

    public void shutdown() {
        redisClient.shutdown();
    }

}

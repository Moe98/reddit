package org.sab.redis;

import io.lettuce.core.api.sync.RedisCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.TimeLimitExceededException;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class RedisTest {

    public static Redis redis;
    public static RedisCommands<String, String> syncCommand;

    final static String key1 = "test1Key";
    final static String key2 = "test2Key";

    /**
     * Rigorous Test :-)
     */

    @BeforeClass
    public static void setUp() {
        RedisTest.redis = new Redis();
        RedisTest.redis.setCommand();
        RedisTest.syncCommand = redis.getConnection().sync();
    }

    public static String getValue(String key) {
        return syncCommand.get(key);
    }

    public static void putValue(String key, String value) {
        syncCommand.set(key, value);
    }

    public static void deleteKey(String key) {
        syncCommand.del(key);
    }

    @Test
    public void putKeyValue() {

        final String key = key1;
        final String value = "This is a test value :)";

        try {
            redis.setKeyVal(key, value);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        String valInRedis = getValue(key);

        assertEquals(value, valInRedis);
    }

    @Test
    public void getValueFromKey() {
        final String key = key2;
        final String value = "Were you able to get me?";
        String valueRetrieved = "";

        putValue(key, value);

        try {
             valueRetrieved = redis.getKeyVal(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        assertEquals(value, valueRetrieved);
    }

    @Test
    public void setArrWithValues() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            redis.setArr(key, values);
            List<String> returnedValues = redis.getArrRange(key, 0, -1);

            assertEquals(returnedValues.size(), values.length);
            IntStream.range(0, returnedValues.size()).
                    forEach(index -> assertEquals(returnedValues.get(index), values[index]));
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKey(key);
    }

    @Test
    public void appendToArr() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            // Set the initial array.
            long initialValuesCount = redis.setArr(key, values);

            // Append to the same array.
            long updatedValuesCount = redis.appendToArr(key, "Epsilon");

            List<String> returnedValues = redis.getArrRange(key, 0, -1);

            assertEquals(initialValuesCount + 1, updatedValuesCount);
            assertEquals(returnedValues.size(), values.length + 1);
            assertEquals(returnedValues.get((int) updatedValuesCount - 1), "Epsilon");
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKey(key);
    }

    @Test
    public void getArrLength() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            redis.setArr(key, values);

            long length = redis.getArrLength(key);

            assertEquals((int) length, values.length);
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKey(key);
    }

    @Test
    public void getLengthForArrDoesNotExist() {
        String key = "key";

        try {
            long length = redis.getArrLength(key);

            assertEquals((int) length, 0);
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {

        deleteKey(key1);
        deleteKey(key2);

        RedisTest.redis.closeConnection();
        RedisTest.redis.shutdown();
    }
}

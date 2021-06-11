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

    public static void deleteKeys(String... keys) {
        syncCommand.del(keys);
    }

    public static long setArr(String arrName, String... values) {
        return syncCommand.rpush(arrName, values);
    }

    public static List<String> getArrRange(String arrName, int start, int stop) {
        return syncCommand.lrange(arrName, start, stop);
    }

    @AfterClass
    public static void tearDown() {

        RedisTest.redis.closeConnection();
        RedisTest.redis.shutdown();
    }

    @Test
    public void putKeyValue() {

        final String key = "key1";
        final String value = "This is a test value :)";

        String status = "";

        try {
            status = redis.setKeyVal(key, value);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        String valInRedis = getValue(key);

        deleteKeys(key);

        assertEquals("OK", status);
        assertEquals(value, valInRedis);
    }

    @Test
    public void getValueFromKey() {
        final String key = "key2";
        final String value = "Were you able to get me?";
        String valueRetrieved = "";

        putValue(key, value);

        try {
            valueRetrieved = redis.getKeyVal(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        deleteKeys(key);

        assertEquals(value, valueRetrieved);
    }

    @Test
    public void checkKeyExists() {
        final String key = "key3";
        final String value = "testVal";

        putValue(key, value);

        long numKeysExist = -1;

        try {
            numKeysExist = redis.existsKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        deleteKeys(key);

        assertEquals(1, numKeysExist);
    }

    @Test
    public void checkMultipleKeyExists() {
        final String key1 = "key4";
        final String key2 = "key5";
        final String key3 = "key6";

        final String value = "testVal";

        putValue(key1, value);
        putValue(key2, value);
        putValue(key3, value);

        long numKeysExist = -1;

        try {
            numKeysExist = redis.existsKey(key1, key2, key3);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        deleteKeys(key1, key2, key3);

        assertEquals(3, numKeysExist);
    }

    @Test
    public void checkKeyDoesNotExist() {

        final String key = "notAKey";

        long numKeysExist = -1;

        try {
            numKeysExist = redis.existsKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        assertEquals(0, numKeysExist);
    }

    @Test
    public void checkArrExists() {

    }

    @Test
    public void checkMultipleArrExists() {

    }

    @Test
    public void checkMultipleKeyValueArrExists() {

    }

    @Test
    public void deleteKeyValue() {

        final String key = "key7";
        final String value = "value";

        putValue(key, value);
        String inRedis = getValue(key);
        assertEquals(value, inRedis);

        long numKeysDeleted = -1;

        try {
            numKeysDeleted = redis.deleteKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        String deletedVal = getValue(key);

        assertEquals(1, numKeysDeleted);
        assertNull(deletedVal);

    }

    @Test
    public void deleteMultipleKeyValue() {
        final String key1 = "key8";
        final String key2 = "key9";
        final String key3 = "key10";

        final String value = "value";

        putValue(key1, value + "1");
        String inRedis = getValue(key1);
        assertEquals(value + "1", inRedis);

        putValue(key2, value + "2");
        inRedis = getValue(key2);
        assertEquals(value + "2", inRedis);

        putValue(key3, value + "3");
        inRedis = getValue(key3);
        assertEquals(value + "3", inRedis);

        long numKeysDeleted = -1;

        try {
            numKeysDeleted = redis.deleteKey(key1, key2, key3);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        assertEquals(3, numKeysDeleted);

        String deletedVal1 = getValue(key1);
        String deletedVal2 = getValue(key2);
        String deletedVal3 = getValue(key3);

        assertNull(deletedVal1);
        assertNull(deletedVal2);
        assertNull(deletedVal3);

    }

    @Test
    public void deleteKeyDoesNotExist() {

        final String key = "notAKey";

        long numKeysDeleted = -1;

        try {
            numKeysDeleted = redis.deleteKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        assertEquals(0, numKeysDeleted);
    }

    @Test
    public void deleteArr() {

    }

    @Test
    public void deleteMultipleArr() {

    }

    @Test
    public void deleteMultipleKeyValueAndArr() {

    }

    @Test
    public void keyExpiresAfter1Min() {
        // get value after 10 seconds --> should exist
        // get value after 60 seconds --> should not exist
    }

    @Test
    public void setMultipleKeyValues() {

    }

    @Test
    public void setArrWithValues() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            redis.setArr(key, values);
            List<String> returnedValues = getArrRange(key, 0, -1);

            assertEquals(returnedValues.size(), values.length);
            IntStream.range(0, returnedValues.size()).
                    forEach(index -> assertEquals(returnedValues.get(index), values[index]));
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKeys(key);
    }

    @Test
    public void appendToArr() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            // Set the initial array.
            long initialValuesCount = setArr(key, values);

            // Append to the same array.
            long updatedValuesCount = redis.appendToArr(key, "Epsilon");

            List<String> returnedValues = getArrRange(key, 0, -1);

            assertEquals(initialValuesCount + 1, updatedValuesCount);
            assertEquals(returnedValues.size(), values.length + 1);
            assertEquals(returnedValues.get((int) updatedValuesCount - 1), "Epsilon");
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKeys(key);
    }

    @Test
    public void getArrLength() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            setArr(key, values);

            long length = redis.getArrLength(key);

            assertEquals((int) length, values.length);
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKeys(key);
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

    @Test
    public void getArrRange() {
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        try {
            setArr(key, values);

            // Should return Luji.
            List<String> lastElement = redis.getArrRange(key, 2, 2);
            // Should return Manta, Luji.
            List<String> partialArray = redis.getArrRange(key, 1, 2);

            assertEquals(lastElement.size(), 1);
            assertEquals(partialArray.size(), 2);

            assertEquals(lastElement.get(0), "Luji");
            assertEquals(partialArray.get(0), "Manta");
            assertEquals(partialArray.get(1), "Luji");
        } catch (TimeLimitExceededException e) {
            fail();
            e.printStackTrace();
        }

        deleteKeys(key);
    }
}

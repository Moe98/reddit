package org.sab.redis;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.TimeLimitExceededException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    @AfterClass
    public static void tearDown() {
        connection.close();
        RedisTest.redis.closeConnection();
        RedisTest.redis.shutdown();
    }

    @After
    public void deleteAllKeys() {
        System.out.println("After");
        final String key = "key";
        final String key1 = "key1";
        final String key2 = "key2";
        final String key3 = "key3";

        deleteKeys(key, key1, key2, key3);
    }

    /*
     * auxiliary methods
     */
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


    @Test
    public void putKeyValue() {

        final String key = "key";
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
        final String key = "key";
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
        final String key = "key";
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
        final String key1 = "key1";
        final String key2 = "key2";
        final String key3 = "key3";

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

        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};
        setArr(key, values);

        long numExists = -1;
        try {
            numExists = redis.existsKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        assertEquals(1, numExists);

        deleteKeys(key);
    }

    @Test
    public void checkMultipleArrExists() {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";

        String[] values = new String[]{"Moe", "Manta", "Luji"};
        setArr(key1, values);
        setArr(key2, values);
        setArr(key3, values);

        long numExists = -1;
        try {
            numExists = redis.existsKey(key1, key2, key3);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        assertEquals(3, numExists);

        deleteKeys(key1, key2, key3);
    }

    @Test
    public void checkMultipleKeyValueArrExists() {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        String key4 = "key4";
        String key5 = "key5";
        String key6 = "key6";
        String value = "val";

        setArr(key1, values);
        setArr(key2, values);
        setArr(key3, values);

        putValue(key4, value);
        putValue(key5, value);
        putValue(key6, value);

        long numExists = -1;
        try {
            numExists = redis.existsKey(key1, key2, key3, key4, key5, key6);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        assertEquals(6, numExists);

        deleteKeys(key1, key2, key3, key4, key5, key6);
    }

    @Test
    public void deleteKeyValue() {

        final String key = "key";
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
        final String key1 = "key1";
        final String key2 = "key2";
        final String key3 = "key3";

        final String value = "value";

        putValue(key1, value + "1");
        putValue(key2, value + "2");
        putValue(key3, value + "3");

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
        String key = "key";
        String[] values = new String[]{"Moe", "Manta", "Luji"};
        setArr(key, values);

        long numDeleted = -1;
        try {
            numDeleted = redis.deleteKey(key);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        List<String> arr = getArrRange(key, 0, -1);

        ArrayList<?> empty = new ArrayList<>();
        assertEquals(1, numDeleted);
        assertEquals(empty, arr);
    }

    @Test
    public void deleteMultipleArr() {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String[] values = new String[]{"Moe", "Manta", "Luji"};

        setArr(key1, values);
        setArr(key2, values);
        setArr(key3, values);

        long numDeleted = -1;
        try {
            numDeleted = redis.deleteKey(key1, key2, key3);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        List<String> arr1 = getArrRange(key1, 0, -1);
        List<String> arr2 = getArrRange(key2, 0, -1);
        List<String> arr3 = getArrRange(key3, 0, -1);

        assertEquals(3, numDeleted);

        ArrayList<?> empty = new ArrayList<>();
        assertEquals(empty, arr1);
        assertEquals(empty, arr2);
        assertEquals(empty, arr3);

    }

    @Test
    public void deleteMultipleKeyValueAndArr() {
        final String key1 = "key1";
        final String key2 = "key2";
        final String key3 = "key3";

        final String key4 = "key4";
        final String key5 = "key5";
        final String key6 = "key6";

        final String value = "value";

        String[] values = new String[]{"Moe", "Manta", "Luji"};

        setArr(key1, values);
        setArr(key2, values);
        setArr(key3, values);

        putValue(key4, value);
        putValue(key5, value);
        putValue(key6, value);

        long numDeleted = -1;
        try {
            numDeleted = redis.deleteKey(key1, key2, key3, key4, key5, key6);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        assertEquals(6, numDeleted);

        List<String> arr1 = getArrRange(key1, 0, -1);
        List<String> arr2 = getArrRange(key2, 0, -1);
        List<String> arr3 = getArrRange(key3, 0, -1);

        ArrayList<?> empty = new ArrayList<>();
        assertEquals(empty, arr1);
        assertEquals(empty, arr2);
        assertEquals(empty, arr3);

        String deletedVal1 = getValue(key4);
        String deletedVal2 = getValue(key6);
        String deletedVal3 = getValue(key3);

        assertNull(deletedVal1);
        assertNull(deletedVal2);
        assertNull(deletedVal3);

    }

    @Test(timeout = 15000)
    public void keyExpiresAfter1Min() {
        String key = "key";
        String value = "value";

        long seconds = 10;

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        Runnable assertExists = () -> {
            try {
                String valRetrieved = redis.getKeyVal(key);
                assertEquals(value, valRetrieved);
            } catch (TimeLimitExceededException e) {
                fail(e.getMessage());
            }
        };

        Runnable assertDoesNotExist = () -> {
            try {
                String valRetrieved = redis.getKeyVal(key);
                assertNull(value, valRetrieved);
            } catch (TimeLimitExceededException e) {
                fail(e.getMessage());
            }
        };

        try{
            redis.setKeyVal(key, value);
            redis.expireKey(key, seconds);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        // get value after half of delay --> should exist
        final ScheduledFuture<?> existsHandle = ses.schedule(assertExists, seconds/2, TimeUnit.SECONDS);

        // get value after delay --> should not exist
        final ScheduledFuture<?> doesNotExistHandle = ses.schedule(assertDoesNotExist, seconds, TimeUnit.SECONDS);

        while(!(existsHandle.isDone() && doesNotExistHandle.isDone()));
    }

    @Test
    public void setMultipleKeyValues() {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";

        String value = "value";

        ArrayList<String> keys = new ArrayList<>(Arrays.asList(key1, key2, key3));
        ArrayList<String> values = new ArrayList<>(Arrays.asList(value, value, value));

        try {
            redis.setAllKeyVal(keys, values);
        } catch (TimeLimitExceededException e) {
            fail(e.getMessage());
        }

        String value1 = getValue(key1);
        String value2 = getValue(key2);
        String value3 = getValue(key3);

        assertEquals(value, value1);
        assertEquals(value, value2);
        assertEquals(value, value3);

        deleteKeys(key1, key2, key3);
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
            fail(e.getMessage());
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
            fail(e.getMessage());
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
            fail(e.getMessage());
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
            fail(e.getMessage());
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
            fail(e.getMessage());
            e.printStackTrace();
        }

        deleteKeys(key);
    }
}

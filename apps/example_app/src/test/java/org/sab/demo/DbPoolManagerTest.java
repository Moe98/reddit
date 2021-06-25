package org.sab.demo;

import com.arangodb.ArangoDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.databases.PoolDoesNotExistException;
import org.sab.service.databases.DBConfig;
import org.sab.service.managers.DBPoolManager;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DbPoolManagerTest {
    static DBPoolManager dbPoolManager;
    final static String arangoClassPath = "org.sab.arango.Arango";
    final static int numConnections = 7;
    static Class<?> clazz;
    static Arango arango = null;
    static HashMap<String, DBConfig> requiredDbs;

    @BeforeClass
    public static void setup() {

        requiredDbs = new HashMap<>();
        requiredDbs.put(arangoClassPath, new DBConfig(numConnections));

        dbPoolManager = new DBPoolManager(requiredDbs);

        try {
            clazz = Class.forName(arangoClassPath);
        } catch (ClassNotFoundException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

    }

    @AfterClass
    public static void teardown() {
        arango = null;
        dbPoolManager = null;
    }

    @Test
    public void T01_initDbClassesTest() {
        try {
            dbPoolManager.initDbClasses();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        arango = (Arango) requiredDbs.get(arangoClassPath).getClient();
        assertNotNull(arango);
    }

    @Test
    public void T02_initDbPoolTest() {
        System.out.println("Arango: " + arango);
        dbPoolManager.initDbPool();

        ArangoDB pool = null;
        try {
            Field f = clazz.getDeclaredField("arangoDB"); //NoSuchFieldException
            f.setAccessible(true);
            pool = (ArangoDB) f.get(arango);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        assertNotNull(pool);
        // TODO can't find method to get the number of DB connections from pool
//        assertEquals(pool.getConnections(), numConnections);
    }

    @Test
    public void T03_setMaxConnection() {
        try {
            dbPoolManager.setMaxDbConnectionCount(arango.getName(), 20);
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }

        assertEquals(requiredDbs.get(arangoClassPath).getConnectionCount(),20);
        // TODO can't find method to get the number of DB connections from pool
//        assertEquals(pool.getConnections(), 20);
    }

    @Test
    public void T04_destroyPool() {
        dbPoolManager.releaseDbPools();
        ArangoDB pool = null;
        try {
            Field f = clazz.getDeclaredField("arangoDB"); //NoSuchFieldException
            f.setAccessible(true);
            pool = (ArangoDB) f.get(arango);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        assertNull(pool);
    }
}

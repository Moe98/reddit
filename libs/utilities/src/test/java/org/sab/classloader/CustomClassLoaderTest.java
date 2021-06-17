package org.sab.classloader;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CustomClassLoaderTest {


    // Uses no-args constructor
    private static Object invokeClassMethod(Class<?> clazz, String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Object obj = clazz.getDeclaredConstructor().newInstance();
        final Method method = clazz.getDeclaredMethod(methodName);
        return method.invoke(obj);
    }

    @Test
    public void loadClassByName() {
        final String className = "org.sab.classloader.Bob";
        final String methodName = "talk";
        final String expectedResult = "Hi";

        try {
            final CustomClassLoader customClassLoader = new CustomClassLoader();
            final Class<?> clazz = customClassLoader.findClass(className);
            assertEquals(expectedResult, invokeClassMethod(clazz, methodName));

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void loadClassFromResourcesByTwoDifferentLoaders() {
        final CustomClassLoader customClassLoader = new CustomClassLoader();
        final String className = "org.sab.classloader.Alice";
        final String methodName = "run";
        final String expectedResult = "Running";

        try {
            final Class<?> clazz = Class.forName(className, true, customClassLoader);
            assertEquals(expectedResult, invokeClassMethod(clazz, methodName));
        } catch (ClassNotFoundException e) {
            fail("Class not loaded from disk.");
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            fail(e.getMessage());
        }

        try {
            final Class<?> clazz = Class.forName(className);
            assertEquals(expectedResult, invokeClassMethod(clazz, methodName));
        } catch (ClassNotFoundException e) {
            fail("Class could not be loaded by the current loader.");
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

}

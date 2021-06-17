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

}

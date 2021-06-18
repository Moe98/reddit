package org.sab.classes;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReaderTest {

    // Uses no-args constructor
    private static Object invokeClassMethod(Class<?> clazz, String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Object obj = clazz.getDeclaredConstructor().newInstance();
        final Method method = clazz.getDeclaredMethod(methodName);
        return method.invoke(obj);
    }

    private static Path getResourcePath(String resource) {
        final URL url = ReaderTest.class.getClassLoader().getResource(resource);

        if (url == null) {
            return null;
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }


    @Test
    public void readUpdateRemoveClass() {
        final String className = "org.sab.classloader.Charlie";
        final String methodName = "jump";

        // Assert that class is not found by the current class loader
        try {
            Class.forName(className);
            fail("Class should not have been loaded.");
        } catch (ClassNotFoundException e) {
            // Expected.
        }

        // Two versions of the same class
        final Path flyingPath = getResourcePath("CharlieFlying");
        final Path hoppingPath = getResourcePath("CharlieHopping");

        final List<Path> paths = List.of(flyingPath, hoppingPath);

        try {
            paths.forEach(Objects::requireNonNull);
        } catch (NullPointerException e) {
            fail(e.getMessage());
        }

        // Expected output for each version
        final List<String> expected = List.of("Flying", "Hopping");

        final int loadCount = 4;

        // Alternate between versions
        for (int i = 0; i < loadCount; i++) {
            Class<?> clazz = null;
            try {
                final byte[] b = Reader.getBytes(paths.get(i % 2));
                ClassRegistry.getInstance().addClassByBytes(className, b);
                clazz = ClassRegistry.getInstance().getClass(className);
            } catch (IOException e) {
                fail("Class file not found.");
            } catch (ClassNotFoundException e) {
                fail("Class failed to be added to registry.");
            }

            try {
                assertEquals(expected.get(i % 2), invokeClassMethod(clazz, methodName));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                fail(e.getMessage());
            }
        }

        // Remove class
        ClassRegistry.getInstance().removeClass(className);

        // Assert that class is no longer available
        try {
            ClassRegistry.getInstance().getClass(className);
            fail("Class should not have available.");
        } catch (ClassNotFoundException e) {
            // Expected.
        }
    }

    @Test
    public void loadClassByName() {
        final String className = "org.sab.classloader.Bob";
        final String methodName = "talk";
        final String expectedResult = "Hi";

        try {
            ClassRegistry.getInstance().addClassByName(className);
            final Class<?> clazz = ClassRegistry.getInstance().getClass(className);
            assertEquals(expectedResult, invokeClassMethod(clazz, methodName));
        } catch (ClassNotFoundException e) {
            fail("Class failed to be added to registry.");
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void loadClassFromResourcesByName() {
        final String className = "org.sab.classloader.Alice";
        final String methodName = "run";
        final String expectedResult = "Running";

        try {
            ClassRegistry.getInstance().addClassByName(className);
            final Class<?> clazz = ClassRegistry.getInstance().getClass(className);
            assertEquals(expectedResult, invokeClassMethod(clazz, methodName));
        } catch (ClassNotFoundException e) {
            fail("Class failed to be added to registry.");
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            fail(e.getMessage());
        }
    }
}

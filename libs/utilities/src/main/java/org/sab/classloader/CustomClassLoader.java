package org.sab.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CustomClassLoader extends ClassLoader {
    private static final String CLASS_EXT = ".class";
    private static final char PACKAGE_SEPARATOR = '.';

    private static byte[] loadClassFileByName(String className) throws IOException {
        byte[] buffer;

        try (final InputStream inputStream = getClassInputStream(className)) {
            if (inputStream == null) {
                throw new IOException("Class file not found.");
            }
            buffer = inputStream.readAllBytes();
        }

        return buffer;
    }

    private static InputStream getClassInputStream(String className) {
        final String path = className.replace(PACKAGE_SEPARATOR, File.separatorChar) + CLASS_EXT;
        return CustomClassLoader.class.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        final byte[] b;
        try {
            b = loadClassFileByName(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("Class could not be read from disk.", e);
        }
        return defineClass(name, b, 0, b.length);
    }
}
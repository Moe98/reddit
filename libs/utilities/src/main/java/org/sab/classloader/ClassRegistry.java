package org.sab.classloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassRegistry {
    private static final ClassRegistry instance = new ClassRegistry();
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    private ClassRegistry() {
    }

    public static ClassRegistry getInstance() {
        return instance;
    }

    public Class<?> getClass(String name) throws ClassNotFoundException {
        final Class<?> clazz = classes.get(name);

        if (clazz == null) {
            throw new ClassNotFoundException("Class not found in registry.");
        }

        return clazz;
    }

    public Class<?> addClassByName(String name) throws ClassNotFoundException {
        final Class clazz = ByteClassLoader.loadClassByName(name);
        classes.put(name, clazz);
        return clazz;
    }

    public Class<?> addClassByBytes(String name, byte[] b) {
        final Class clazz = ByteClassLoader.loadClassFromBytes(name, b);
        classes.put(name, clazz);
        return clazz;
    }

    public void removeClass(String name) {
        classes.remove(name);
    }

}

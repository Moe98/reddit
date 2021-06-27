package org.sab.service.managers;

import org.sab.classes.ClassRegistry;
import org.sab.service.ServiceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ClassManager {
    private final ClassRegistry classRegistry = ClassRegistry.getInstance();
    private final Map<String, String> commandMap = new ConcurrentHashMap<>();

    private static Properties readPropertiesFile(String path) throws IOException {
        final InputStream inputStream = ClassManager.class.getClassLoader().getResourceAsStream(path);
        final Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }

    public void init() throws IOException, ClassNotFoundException {
        loadCommandMap();
        loadCommandClasses();
    }

    private void loadCommandMap() throws IOException {
        final Properties commandProperties = readPropertiesFile(ServiceConstants.COMMAND_MAP_FILENAME);
        for (final String key : commandProperties.stringPropertyNames()) {
            commandMap.put(key, commandProperties.getProperty(key));
        }
    }

    private void loadCommandClasses() throws ClassNotFoundException {
        boolean someClassWereNotFound = false;
        for (final String className : commandMap.values()) {
            try {
                classRegistry.addClassByName(className);
            } catch (ClassNotFoundException e) {
                someClassWereNotFound = true;
            }
        }

        if (someClassWereNotFound) {
            throw new ClassNotFoundException("Some classes were not found.");
        }
    }

    public Class<?> getCommand(String functionName) throws ClassNotFoundException {
        final String className = commandMap.get(functionName);
        if(className == null) {
            throw new ClassNotFoundException("Class not found in command map.");
        }
        return classRegistry.getClass(className);
    }


    public void addCommand(String functionName, String className, byte[] b) {
        commandMap.put(functionName, className);
        classRegistry.addClassByBytes(className, b);
    }

    public void updateCommand(String functionName, String className, byte[] b) {
        addCommand(functionName, className, b);
    }

    public void deleteCommand(String functionName) {
        final String className = commandMap.get(functionName);
        commandMap.remove(functionName);
        classRegistry.removeClass(className);
    }

}

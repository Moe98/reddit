package org.sab.service;

import org.sab.strings.StringManipulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class uses reflection to save a reference to the classes without defining them in the code
 * This is used in the command pattern to access classes
 */

public class ConfigMap {

    private static ConfigMap instance = new ConfigMap();

    private ConcurrentHashMap<String, String> cmdMap;

    private ConfigMap() {
        cmdMap = new ConcurrentHashMap<>();

    }

    public static ConfigMap getInstance() {
        return instance;
    }

    public void instantiate(InputStream inputStream) throws IOException {
        final Properties properties = new Properties();
        properties.load(inputStream);

        for (final String key : properties.stringPropertyNames()) {
            cmdMap.put(key, properties.get(key).toString());
        }
    }

    public Class<?> getClass(String command) throws ClassNotFoundException {
        final String className = cmdMap.get(command);
        if (className == null) {
            throw new ClassNotFoundException();
        }
        return Class.forName(className);
    }

    public void replaceClassWith(String key, String newClass) {
        cmdMap.put(key, newClass);
    }

    /**
     * @param commandName String representing the name of the .class file (e.g org.sab.user.signUp)
     */
    public void addCommand(String commandName) {
        String[] splittedCommandName = commandName.split(".");
        String functionName = StringManipulation.pascalToScreamingCase(splittedCommandName[splittedCommandName.length - 1]);
        addCommand(functionName, commandName);
    }

    private void addCommand(String functionName, String commandName) {
        cmdMap.put(functionName, commandName);
    }

    public void deleteCommand(String functionName) {
        cmdMap.remove(functionName);
    }
}

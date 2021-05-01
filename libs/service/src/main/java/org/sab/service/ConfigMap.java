package org.sab.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class uses reflection to save a reference to the classes without defining them in the code
 * This is used in the command pattern to access classes
 */

public class ConfigMap {
    // TODO change this to config file.

    private static ConfigMap instance = new ConfigMap();

    private ConcurrentHashMap<String, String> cmdMap;

    private ConfigMap() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("HELLO_WORLD", "org.sab.demo.commands.HelloWorld");
        cmdMap.put("GOOD_BYE_WORLD", "org.sab.demo.commands.GoodByeWorld");
        cmdMap.put("SEARCH_THREAD", "org.sab.search.commands.SearchThread");
        cmdMap.put("SEARCH_SUBTHREAD", "org.sab.search.commands.SearchSubThread");

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
        if(className == null){
            throw new ClassNotFoundException();
        }
        return Class.forName(className);
    }

    public void replaceClassWith(String key, String newClass) {
        cmdMap.put(key, newClass);
        System.out.println("replaced");
    }
}

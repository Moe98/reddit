package org.sab.service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class uses reflection to save a reference to the classes without defining them in the code
 * This is used in the command pattern to access classes
 */

public class ConfigMap {
    // TODO change this to config file.
    private static ConcurrentHashMap<String, String> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("HELLO_WORLD", "org.sab.demo.commands.HelloWorld");
        cmdMap.put("GOOD_BYE_WORLD", "org.sab.demo.commands.GoodByeWorld");
        cmdMap.put("UPDATE_POPULAR_THREADS", "org.sab.recommendation.commands.UpdatePopularThreads");
        cmdMap.put("GET_POPULAR_THREADS", "org.sab.recommendation.commands.GetPopularThreads");
        cmdMap.put("UPDATE_POPULAR_SUBTHREADS", "org.sab.recommendation.commands.UpdatePopularSubThreads");
        cmdMap.put("GET_POPULAR_SUBTHREADS", "org.sab.recommendation.commands.GetPopularSubThreads");
    }

    public static Class<?> getClass(String command) throws ClassNotFoundException {
        String classPath = cmdMap.get(command);
        if(classPath != null)
            return Class.forName(classPath);
        return null;
    }

    public static void replaceClassWith(String key, String newClass) {
        cmdMap.put(key, newClass);
        System.out.println("replaced");
    }
}

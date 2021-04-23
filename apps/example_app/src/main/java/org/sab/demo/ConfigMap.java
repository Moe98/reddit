package org.sab.demo;

//import org.sab.demo.commands.GoodByeWorld;
//import org.sab.demo.commands.HelloWorld;

import java.util.concurrent.ConcurrentHashMap;

public class ConfigMap {
    // TODO change this to config file.
    private static ConcurrentHashMap<String, String> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("HELLO_WORLD", "org.sab.demo.commands.HelloWorld");
        cmdMap.put("GOOD_BYE_WORLD", "org.sab.demo.commands.GoodByeWorld");
    }

    public static Class<?> getClass(String command) throws ClassNotFoundException {
        return Class.forName(cmdMap.get(command));
    }

    public static void replaceClassWith(String key, String newClass) {
        cmdMap.put(key, newClass);
        System.out.println("replaced");
    }
}
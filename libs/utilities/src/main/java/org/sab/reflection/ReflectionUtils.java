package org.sab.reflection;

import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Method getMethod(Class clas, String methodName) {
        try {
            Method[] methods = clas.getMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(methodName)) {
                    return method;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}

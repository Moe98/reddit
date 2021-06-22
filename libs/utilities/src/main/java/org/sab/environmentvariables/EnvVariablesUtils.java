package org.sab.environmentvariables;

public class EnvVariablesUtils {

    public static String getOrDefaultEnvVariable(String envVariable, String defaultValue) {
        String value = System.getenv(envVariable);
        return value == null ? defaultValue : value;
    }
}

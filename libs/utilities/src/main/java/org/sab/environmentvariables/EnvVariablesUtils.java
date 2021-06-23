package org.sab.environmentvariables;

import java.util.Objects;

public class EnvVariablesUtils {

    public static String getOrDefaultEnvVariable(String envVariable, String defaultValue) {
        String value = System.getenv(envVariable);
        return value == null ? defaultValue : value;
    }

    /**
     * gets an environment variable if exists and throws a null pointer exception if it's not found
     *
     * @return System.getenv {@code envVariable}
     * @throws NullPointerException if {@code envVariable} is not found
     */
    public static String getEnvOrThrow(String envVariable) {
        return Objects.requireNonNull(System.getenv(envVariable), String.format("The Environment Variable \"%s\" cannot be null", envVariable));
    }

}

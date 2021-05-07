package org.sab.functions;

public class Utilities {
    public static boolean isDevelopmentMode() {
        String mode = System.getenv("ENV_TYPE");
        return mode != null && mode.equals("Development");
    }
}

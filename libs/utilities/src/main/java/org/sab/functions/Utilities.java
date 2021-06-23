package org.sab.functions;

public class Utilities {
    private Utilities() {
    }

    public static boolean isDevelopmentMode() {
        String mode = System.getenv("ENV_TYPE");
        return mode != null && mode.equals("Development");
    }
    public static boolean inContainerizationMode() {
        String mode = System.getenv("ENV_TYPE");
        return mode != null && mode.equals("Staging");
    }

    public static String formatUUID(String UUID) {
        return UUID.replaceAll("[-]", "");
    }
}

package org.sab.service;

import org.json.JSONObject;

/**
 * Abstract class Command that is extended by all command classes.
 */

public abstract class Command {
    private static double classVersion = 1.0;

    // The function that will be invoked by all classes implementing Command.
    public abstract String execute(JSONObject request);

    public static double getClassVersion() {
        return classVersion;
    }

    public static void setClassVersion(double classVersion) {
        Command.classVersion = classVersion;
    }

}

package org.sab.service;

import org.json.JSONObject;

import java.util.concurrent.Callable;

/**
 * Abstract class Command that is extended by all command classes
 */

public abstract class Command implements Callable<String> {
    private JSONObject request;
    private static double classVersion = 1.0;

    // function implemented by all command classes which will be invoked by the app
    public abstract String execute(JSONObject request) throws Exception;

    public static double getClassVersion() {
        return classVersion;
    }

    public void setClassVersion(double classVersion) {
        classVersion = classVersion;
    }

    public void setRequest(JSONObject request) {
        this.request = request;
    }

    // TODO do we need to invoke call?
    @Override
    public String call() {
        System.out.println("Executing with: " + request);
        String res = "";
        try {
            res = execute(this.request);
            System.out.println("Execute returned: " + res);
            //return res;
        } catch (Exception e) {
            e.printStackTrace();
            return "failed to execute";
        }
        return res;
    }
}

package org.sab.service;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 *
 * Abstract class Command that is extended by all command classes
 *
 */

public abstract class Command implements Callable {
    HashMap<String, String> map;

    // function implemented by all command classes which will be invoked by the app
    public abstract String execute() throws Exception;

    @Override
    public String call() {
        try {
            return execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return null;
        }
    }
}

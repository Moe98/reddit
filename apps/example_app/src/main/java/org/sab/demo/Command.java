package org.sab.demo;

import java.util.HashMap;
import java.util.concurrent.Callable;

public abstract class Command implements Callable {
    HashMap<String, String> map;

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
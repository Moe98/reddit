package org.sab.demo.commands;

import org.sab.service.Command;

import java.util.concurrent.Callable;

public class HelloWorld extends Command implements Callable {
    private static double classVersion = 1.0;

    public static double getClassVersion() {
        return classVersion;
    }

    @Override
    public String execute() throws Exception {
        String message = "{\"msg\":\"Hello World\"}";
        return message;
    }
}
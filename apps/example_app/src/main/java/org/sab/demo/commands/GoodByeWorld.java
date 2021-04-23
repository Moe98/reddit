package org.sab.demo.commands;

import org.sab.demo.Command;

import java.util.concurrent.Callable;

public class GoodByeWorld extends Command implements Callable {
    private static double classVersion = 1.0;

    public static double getClassVersion() {
        return classVersion;
    }

    @Override
    public String execute() throws Exception {
        String message = "{\"msg\":\"GoodBye World\"}";
        return message;
    }
}

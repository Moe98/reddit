package org.sab.demo;

import org.sab.service.ConfigMap;
import org.sab.service.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ExampleApp extends Service {

    // TODO get this from config file
    private static final String EXAMPLE_APP_QUEUE = "EXAMPLE_APP_REQ";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConfigMap.instantiate();
        getThreadPool(10);
        listenOnQueue(EXAMPLE_APP_QUEUE);

    }
}

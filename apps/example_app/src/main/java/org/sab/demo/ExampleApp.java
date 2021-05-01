package org.sab.demo;

import org.sab.service.ConfigMap;
import org.sab.service.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ExampleApp extends Service {

    // TODO get this from config file
    private static final String EXAMPLE_APP_QUEUE = "EXAMPLE_APP_REQ";

    @Override
    public String getAppUriName() {
        return "EXAMPLE_APP";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static void main(String[] args){
        new ExampleApp().start();
    }
}

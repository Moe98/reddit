package org.sab.demo;

import org.sab.service.Service;

public class ExampleApp extends Service {

    public static void main(String[] args) {
        new ExampleApp().start();
    }

    @Override
    public String getAppUriName() {
        return "EXAMPLE_APP";
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }
}

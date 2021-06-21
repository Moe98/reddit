package org.sab.demo;

import org.sab.service.Service;

public class ExampleApp extends Service {

    public static void main(String[] args) {
        ExampleApp a = new ExampleApp();
        a.start();
    }

    @Override
    public String getAppUriName() {
        return "EXAMPLE";
    }



}

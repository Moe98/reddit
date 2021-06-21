package org.sab.demo;

import org.sab.service.Service;

public class ExampleApp extends Service {

    public static void main(String[] args) {
        ExampleApp a = new ExampleApp();
        a.start();
        System.out.println("The required databases for Example App: " + a.requiredDbs);
    }

    @Override
    public String getAppUriName() {
        return "EXAMPLE";
    }



}

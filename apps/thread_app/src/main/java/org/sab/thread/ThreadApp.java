package org.sab.thread;

import org.sab.service.Service;

public class ThreadApp extends Service {

    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";

    public static void main(String[] args) {
        new ThreadApp().start();
    }

    @Override
    public String getAppUriName() {
        return "thread";
    }

}

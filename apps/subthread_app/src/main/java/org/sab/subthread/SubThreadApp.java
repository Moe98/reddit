package org.sab.subthread;

import org.sab.service.Service;

public class SubThreadApp extends Service {

    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";

    public static void main(String[] args) {
        new SubThreadApp().start();
    }

    @Override
    public String getAppUriName() {
        return "thread";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }
}
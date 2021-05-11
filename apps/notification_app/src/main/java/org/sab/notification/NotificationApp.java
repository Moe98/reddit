package org.sab.notification;

import org.sab.service.Service;

public class NotificationApp extends Service {
    public static void main(String[] args) {
        new NotificationApp().start();
    }

    @Override
    public String getAppUriName() {
        return "notification";
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

package org.sab.notification;

import org.sab.service.Service;

public class NotificationApp extends Service {
    public static final String TOKEN = "token";
    public static final String TOKENS_COLLECTION = "userTokens";

    public static void main(String[] args) {
        new NotificationApp().start();
    }

    public static String getNotificationsCollectionName(String user) {
        return "userNotifications/" + user + "/notifications";
    }

    @Override
    public String getAppUriName() {
        return "notification";
    }

}

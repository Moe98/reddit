package org.sab.models;

public enum NotificationAttributes {
    // TODO: change its name to userList instead of registrationToken here and in the sendNotification command
    USERS_LIST("registrationTokens", null),
    TITLE("title",null),
    NOTIFICATION_BODY("notificationBody", null);


    private final String http;
    private final String db;

    NotificationAttributes(String http, String db) {
        this.http = http;
        this.db = db;
    }

    public String getHTTP() {
        return http;
    }

    public String getDb() {
        return db;
    }
}

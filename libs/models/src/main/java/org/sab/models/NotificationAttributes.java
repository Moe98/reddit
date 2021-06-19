package org.sab.models;

public enum NotificationAttributes {
    // TODO: change its name to userList instead of registrationToken here and in the sendNotification command
    USERS_LIST("registrationTokens"),
    TITLE("title"),
    NOTIFICATION_BODY("notificationBody");


    private final String value;

    NotificationAttributes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

package org.sab.models;

public enum NotificationAttributes {

    USERS_LIST("usersList"),
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

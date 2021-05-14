package org.sab.notification;

public class NotificationSendingFailedException extends Exception {
    public NotificationSendingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

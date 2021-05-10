package org.sab.chat.storage.exceptions;

public class InvalidInputException extends Exception {
    static private final String DEFAULT_MESSAGE = "Inputs provided to are invalid.";

    public InvalidInputException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidInputException(Exception e) {
        super(DEFAULT_MESSAGE);
        e.printStackTrace();
    }

    public InvalidInputException(String message) {
        super(message);
    }
}

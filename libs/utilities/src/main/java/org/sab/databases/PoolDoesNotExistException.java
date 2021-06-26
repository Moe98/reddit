package org.sab.databases;

public class PoolDoesNotExistException extends Exception{

    public PoolDoesNotExistException(String message) {
        super(message);
    }

}

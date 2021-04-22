package org.sab.postgres.exceptions;

public class PropertiesNotLoadedException extends Exception {
    static private final String DEFAULT_MESSAGE="Properties wasn't loaded correctly. Check the config file!";
    public PropertiesNotLoadedException(){
        super(DEFAULT_MESSAGE);
    }
    public PropertiesNotLoadedException(Exception e){
        super(DEFAULT_MESSAGE);
        e.printStackTrace();
    }
    public PropertiesNotLoadedException(String message){
        super(message);
    }
}

package org.sab.models.user;

public enum UserAttributes {

    // variables in a user object
    ACTION_MAKER_ID("userId", "UserId"),
    USER_ID("userId", "UserId"),
    IS_DELETED("isDeleted", "IsDeleted"),
    DATE_CREATED("dateCreated", "DateCreated");


    private final String http;
    private final String db;

    UserAttributes(String http, String db) {
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

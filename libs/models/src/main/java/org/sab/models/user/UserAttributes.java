package org.sab.models.user;

public enum UserAttributes {

    // variables in a thread object
    USER_ID("id", "Id"),
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

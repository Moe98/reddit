package org.sab.models;

public enum AuthenticationAttributes {
    AUTHENTICATION_PARAMS("authenticationParams",null),
    IS_AUTHENTICATED("isAuthenticated", null);
;
    private final String http;
    private final String db;

    AuthenticationAttributes(String http, String db) {
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

package org.sab.models;

public enum RequestAttributes {
    BODY("body", null),
    METHOD_TYPE("methodType",null),
    URI_PARAMS("uriParams", null);

    private final String http;
    private final String db;

    RequestAttributes(String http, String db) {
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

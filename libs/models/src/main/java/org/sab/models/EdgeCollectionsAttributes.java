package org.sab.models;

public enum EdgeCollectionsAttributes {
    USER_FOLLOW_THREAD_DATE("Date");

    private final String db;

    EdgeCollectionsAttributes(String db) {
        this.db = db;
    }

    public String getDb() {
        return db;
    }
}

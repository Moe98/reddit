package org.sab.models;

public enum ThreadAttributes {

    // variables in a thread object
    THREAD_NAME("name", "Name"),
    DESCRIPTION("description", "Description"),
    CREATOR_ID("creatorId", "CreatorId"),
    NUM_OF_FOLLOWERS("numOfFollowers", "NumOfFollowers"),
    DATE_CREATED("dateCreated", "DateCreated"),

    // additional request parameters
    ASSIGNER_ID("assignerId", null),
    MODERATOR_ID("moderatorId", null);


    private final String http;
    private final String db;

    ThreadAttributes(String http, String db) {
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

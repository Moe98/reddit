package org.sab.models;

public enum SubThreadAttributes {

    // variables in a subthread object
    SUBTHREAD_ID("id", "Id"),
    PARENT_THREAD_ID("parentThreadId", "ParentThreadId"),
    CREATOR_ID("creatorId", "CreatorId"),

    TITLE("title", "Title"),
    CONTENT("content", "Content"),

    LIKES("likes", "Likes"),
    DISLIKES("dislikes", "Dislikes"),

    HAS_IMAGE("hasImage", "HasImage"),

    DATE_CREATED("dateCreated", "DateCreated"),

    // additional attributes
    ACTION_MAKER_ID("userId", null);

    private final String http;
    private final String db;

    SubThreadAttributes(String http, String db) {
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

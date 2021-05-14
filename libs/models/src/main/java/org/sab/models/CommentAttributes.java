package org.sab.models;

public enum CommentAttributes {

    // variables in a Comment object
    COMMENT_ID("commentId", "CommentId"),
    CREATOR_ID("creatorId", "CreatorId"),

    // TODO parent could be a comment...
    PARENT_SUBTHREAD_ID("parentSubthreadId", "ParentSubthreadId"),
    PARENT_CONTENT_TYPE("parentContentType", "ParentContentType"),

    CONTENT("content", "Content"),
    DATE_CREATED("dateCreated", "DateCreated"),

    LIKES("likes", "Likes"),
    DISLIKES("dislikes", "Dislikes"),

    // additional request parameters
    ACTION_MAKER_ID("userId", null);

    private final String http;
    private final String db;

    CommentAttributes(String http, String db) {
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

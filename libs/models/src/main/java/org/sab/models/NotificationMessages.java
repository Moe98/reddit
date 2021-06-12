package org.sab.models;

public enum NotificationMessages {
    COMMENT_TAG_MSG("You have been tagged in a comment"),
    COMMENT_LIKE_MSG("Someone liked your comment"),
    COMMENT_DISLIKE_MSG( "Someone Disliked your comment"),

    SUBTHREAD_TAG_MSG("You have been tagged in a subthread"),
    SUBTHREAD_LIKE_MSG("Someone liked your subthread"),
    SUBTHREAD_DISLIKE_MSG( "Someone Disliked your subthread");



    private final String msg;

    NotificationMessages(String msg) {
        this.msg = msg;
    }

    public String getMSG() {
        return msg;
    }

}

package org.sab.models;

public enum CollectionNames {

    // model collections
    THREAD("Thread"),
    SUBTHREAD("Subthread"),
    COMMENT("Comment"),
    USER("User"),
    // # Subthread collections
    USER_BOOKMARK_SUBTHREAD("UserBookmarkSubthread"),
    USER_LIKE_SUBTHREAD("UserLikeSubthread"),
    USER_DISLIKE_SUBTHREAD("UserDislikeSubthread"),
    SUBTHREAD_REPORTS("SubthreadReports"),
    USER_FOLLOW_USER("UserFollowUser"),
    USER_MOD_THREAD("UserModThread"),
    // Comments collections
    USER_CREATE_COMMENT("UserCreateComment"),
    CONTENT_COMMENT("ContentComment"),
    USER_LIKE_COMMENT("UserLikeComment"),
    USER_DISLIKE_COMMENT("UserDislikeComment"),
    // Thread Collection
    USER_FOLLOW_THREAD("UserFollowThread"),
    USER_FOLLOW_THREAD_DATE("Date"),
    USER_BOOKMARK_THREAD("UserBookmarkThread"),
    THREAD_CONTAIN_SUBTHREAD("ThreadContainSubThread"),
    USER_BANNED_FROM_THREAD("UserBannedFromThread");

    private final String collectionName;
    CollectionNames(String collectionName) {
        this.collectionName = collectionName;
    }

    public String get() {
        return this.collectionName;
    }

}


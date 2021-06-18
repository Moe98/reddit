package org.sab.models;

public enum NotificationMessages {
    COMMENT_TAG_MSG("You have been tagged in a comment"),
    COMMENT_LIKE_MSG("Someone liked your comment"),
    COMMENT_DISLIKE_MSG( "Someone Disliked your comment"),
    COMMENT_DELETE_MSG("You deleted your comment"),
    COMMENT_CREATE_MSG("Somebody commented on your content"),
    COMMENT_UPDATE_MSG("You have updated your comment"),

    SUBTHREAD_TAG_MSG("You have been tagged in a subthread"),
    SUBTHREAD_LIKE_MSG("Someone liked your subthread"),
    SUBTHREAD_DISLIKE_MSG( "Someone Disliked your subthread"),
    SUBTHREAD_DELETE_MSG("You deleted your subthread"),
    SUBTHREAD_CREATE_MSG("Someone created a subthread on your thread"),
    SUBTHREAD_BOOKMARK_MSG("You have bookmarked a subthread"),
    SUBTHREAD_REMOVE_BOOKMARK_MSG("You have un-bookmarked a subthread"),
    SUBTHREAD_REPORT_MSG("You have reported a subthread"),
    SUBTHREAD_UPDATE_MSG("You have updated your subthread"),

    THREAD_UPDATE_MSG("You updated your thread"),
    THREAD_DELETE_MSG("You deleted your thread"),
    THREAD_CREATE_MSG("You have created a thread"),
    THREAD_BOOKMARK_MSG("You have bookmarked a thread"),
    THREAD_REMOVE_BOOKMARK_MSG("You have un-bookmarked a thread"),
    THREAD_FOLLOW_MSG("You have followed a thread"),
    THREAD_UNFOLLOW_MSG("You have unfollowed a thread"),
    THREAD_MOD_ASSIGNED_MOD_MSG("You have assigned a moderator to a thread"),
    THREAD_USER_IS_MOD_MSG("You have been assigned as a moderator to a thread"),
    THREAD_USER_BANNED_MSG("You have been banned from a thread"),
    THREAD_MOD_BANS_MSG("You have been banned someone from a thread"),

    USER_FOLLOW_USER_MSG("You have followed a user"),
    USER_GOT_FOLLOWED_MSG("Someone followed you"),
    USER_UNFOLLOW_USER_MSG("You have unfollowed a user"),
    USER_GOT_UNFOLLOWED_MSG("Someone unfollowed you"),
    USER_UNBLOCKED_USER_MSG("You have unblocked a user"),
    USER_BLOCKED_USER_MSG("You have blocked a user");



    private final String msg;

    NotificationMessages(String msg) {
        this.msg = msg;
    }

    public String getMSG() {
        return msg;
    }

}

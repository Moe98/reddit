package org.sab.models;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Comment {
    private String id;
    private String parentSubthreadId;
    private String creatorId;
    private long likes;
    private long dislikes;
    private String content;
    private String dateCreated;
    private String parentContentType;

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getParentId() {
        return parentSubthreadId;
    }

    public void setParentId(String parentId) {
        this.parentSubthreadId = parentId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public String getParentContentType() {
        return parentContentType;
    }

    public void setParentContentType(String parentContentType) {
        this.parentContentType = parentContentType;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "parentSubthreadId='" + parentSubthreadId + '\'' +
                ",creatorId='" + creatorId + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", content='" + content + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }

    public JSONObject toJSON() {

        JSONObject comment = new JSONObject();
        comment.put(CommentAttributes.COMMENT_ID.getHTTP(), comment);
        comment.put(CommentAttributes.PARENT_SUBTHREAD_ID.getHTTP(), parentSubthreadId);
        comment.put(CommentAttributes.CREATOR_ID.getHTTP(), creatorId);
        comment.put(CommentAttributes.LIKES.getHTTP(), likes);
        comment.put(CommentAttributes.DISLIKES.getHTTP(), dislikes);
        comment.put(CommentAttributes.CONTENT.getHTTP(), content);
        comment.put(CommentAttributes.DATE_CREATED.getHTTP(), dateCreated);
        comment.put(CommentAttributes.PARENT_CONTENT_TYPE.getHTTP(), parentContentType);
        return comment;
    }
}
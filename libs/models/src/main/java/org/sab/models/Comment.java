package org.sab.models;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Comment {
    private String parentSubthreadId;
    private String creatorId;
    private long likes;
    private long dislikes;
    private String content;
    private String dateCreated;

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
        comment.put("parentId", parentSubthreadId);
        comment.put("creatorId", creatorId);
        comment.put("likes", likes);
        comment.put("dislikes", dislikes);
        comment.put("content", content);
        comment.put("dateCreated", dateCreated);
        return comment;
    }
}
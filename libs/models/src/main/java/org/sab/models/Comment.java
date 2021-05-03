package org.sab.models;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Comment {
    private String creatorId;
    private String parentId;
    private String dateCreated;
    private String content;
    private long likes;
    private long dislikes;

    public String getCreatorId() {
        return creatorId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getContent() {return content;}

    public long getLikes() {return likes;}

    public long getDislikes() {
        return dislikes;
    }

    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public void setParentId(String parentId) { this.parentId = parentId; }

    public void setDateCreated(String dateCreated) {this.dateCreated = dateCreated;}

    public void setContent(String content) {this.content = content;}

    public void setLikes(long likes) {this.likes = likes;}

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "creatorId='" + creatorId + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", content='" + content + '\'' +
                ", parentId='" + parentId + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject comment = new JSONObject();
        comment.put("creatorId",creatorId);
        comment.put("dateCreated",dateCreated);
        comment.put("content",content);
        comment.put("parentId",parentId);
        comment.put("likes",likes);
        comment.put("dislikes",dislikes);
        return comment;
    }
}
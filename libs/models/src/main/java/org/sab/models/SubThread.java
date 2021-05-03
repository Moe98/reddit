package org.sab.models;

import org.json.JSONObject;

public class SubThread {
    private String parentThreadId;
    private String id;
    private String title;
    private String creatorId;
    private int likes;
    private int dislikes;
    private String content;
    private Boolean hasImage;
    private String dateCreated;


    public String getParentThreadId() {
        return parentThreadId;
    }

    public void setParentThreadId(String parentThreadId) {
        this.parentThreadId = parentThreadId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "SubThread:{" +
                "parentThreadId='" + parentThreadId + '\'' +
                ", id='" + id + '\'' +
                ", title=" + title +
                ", creatorId='" + creatorId + '\'' +
                ", likes='" + likes + '\'' +
                ", dislikes='" + dislikes + '\'' +
                ", content='" + content + '\'' +
                ", hasImage='" + hasImage + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject thread = new JSONObject();
        thread.put("name", parentThreadId);
        thread.put("description", id);
        thread.put("creatorId", title);
        thread.put("creatorId", creatorId);
        thread.put("creatorId", likes);
        thread.put("creatorId", dislikes);
        thread.put("creatorId", content);
        thread.put("numOfFollowers", hasImage);
        thread.put("dateCreated", dateCreated);
        return thread;
    }
}

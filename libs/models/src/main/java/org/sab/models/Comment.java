package org.sab.models;

@SuppressWarnings("unused")
public class Comment {
    private final String creatorId;
    private final String parentId;
    private String dateCreated;
    private String content;
    private long likes;
    private long dislikes;

    public Comment(String creatorId, String parentId, String dateCreated, String content, long likes, long dislikes) {
        this.creatorId = creatorId;
        this.parentId = parentId;
        this.dateCreated = dateCreated;
        this.content = content;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getParentId() {
        return parentId;
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
                "creatorId='" + creatorId + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", content='" + content + '\'' +
                ", parentId='" + parentId + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                '}';
    }
}
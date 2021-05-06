package org.sab.models;

import org.json.JSONObject;

public class SubThread {
    private String id;
    private String parentThreadId;
    private String creatorId;
    private String title;
    private String content;
    private int likes;
    private int dislikes;
    private Boolean hasImage;
    private String dateCreated;

    private SubThread(String parentThreadId, String creatorId,
                      String title, String content,
                      Boolean hasImage) {

        this.parentThreadId = parentThreadId;
        this.creatorId = creatorId;
        this.title = title;
        this.content = content;
        this.hasImage = hasImage;
    }

    public static SubThread createNewSubThread(String parentThreadId, String creatorId,
                                            String title, String content,
                                            Boolean hasImage) {

        return new SubThread(parentThreadId, creatorId, title, content, hasImage);
    }

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

        JSONObject subThread = new JSONObject();
        subThread.put(SubThreadAttributes.SUBTHREAD_ID.getHTTP(), id);
        subThread.put(SubThreadAttributes.PARENT_THREAD_ID.getHTTP(), parentThreadId);
        subThread.put(SubThreadAttributes.CREATOR_ID.getHTTP(), creatorId);
        subThread.put(SubThreadAttributes.TITLE.getHTTP(), title);
        subThread.put(SubThreadAttributes.LIKES.getHTTP(), likes);
        subThread.put(SubThreadAttributes.DISLIKES.getHTTP(), dislikes);
        subThread.put(SubThreadAttributes.CONTENT.getHTTP(), content);
        subThread.put(SubThreadAttributes.HAS_IMAGE.getHTTP(), hasImage);
        subThread.put(SubThreadAttributes.DATE_CREATED.getHTTP(), dateCreated);

        return subThread;
    }
}

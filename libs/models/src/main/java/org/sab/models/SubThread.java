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

    
    public SubThread() {
        super();
    }
    
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
    
    // TODO fix duplicate methods
    // id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    // parentThreadId
    public String getParentThreadId() {
        return parentThreadId;
    }
    
    public void setParentThreadId(String parentThreadId) {
        this.parentThreadId = parentThreadId;
    }

    public String getParentThread() {
        return parentThreadId;
    }


    public void setParentThread(String parentThread) {
        this.parentThreadId = parentThread;
    }

    // creatorId;
    public String getCreatorId() {
        return creatorId;
    }

    public String getCreator() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreator(String creator) {
        this.creatorId = creator;
    }

    // title;
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // content;
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // likes;
    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    // dislikes;
    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    // hasImage
    // TODO fix name --> hasImage + getHasImage
    public Boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isHasImage() {
        return hasImage;
    }


    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    // date created
    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDate() {
        return dateCreated;
    }
    
    public void setDate(String date) {
        this.dateCreated = date;
    }
    
    // TODO fix enums
    public static String getCollectionName() {
        return "SubThreads";
    }

    public static String getIdAttributeName() {
        return "_key";
    }

    public static String getParentThreadAttributeName() {
        return "ParentThread";
    }

    public static String getTitleAttributeName() {
        return "Title";
    }
    
    public static String getCreatorAttributeName() {
        return "Creator";
    }
    
    public static String getLikesAttributeName() {
        return "Likes";
    }

    public static String getDislikesAttributeName() {
        return "Dislikes";
    }

    public static String getContentAttributeName() {
        return "Content";
    }

    public static String getHasImageAttributeName() {
        return "HasImage";
    }

    public static String getDateAttributeName() {
        return "Time";
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

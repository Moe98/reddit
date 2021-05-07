package org.sab.models;

@SuppressWarnings("unused")
public class SubThread {
    String id;
    String parentThread;
    String title;
    String creator;
    int likes;
    int dislikes;
    String content;
    boolean hasImage;
    String date;

    public SubThread() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentThread() {
        return parentThread;
    }

    public void setParentThread(String parentThread) {
        this.parentThread = parentThread;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

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
        return "SubThread{" +
                "id='" + id + '\'' +
                ", parentThread='" + parentThread + '\'' +
                ", title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", content='" + content + '\'' +
                ", hasImage=" + hasImage +
                ", date='" + date + '\'' +
                '}';
    }
}

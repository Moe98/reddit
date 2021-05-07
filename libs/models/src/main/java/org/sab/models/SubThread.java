package org.sab.models;

@SuppressWarnings("unused")
public class SubThread {
    public String id;
    public String parentThread;
    public String title;
    public String creator;
    public int likes;
    public int dislikes;
    public String content;
    public boolean hasImage;
    public String date;

    public SubThread() {
        super();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParentThread(String parentThread) {
        this.parentThread = parentThread;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
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

package org.sab.models;

public class SubThread {

    String _key;
    String parentThread;
    String title;
    String creator;
    long likes;
    long dislikes;
    String content;
    boolean hasImage;
    String time;

    public SubThread(){
        super();
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

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public void setTime(String time) {
        time = time;
    }

    public String getParentThread() {
        return parentThread;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public long getLikes() {
        return likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public String getContent() {
        return content;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public String getTime() {
        return time;
    }

    public String get_key() {
        return _key;
    }

    public void set_key(String _key) {
        this._key = _key;
    }

    @Override
    public String toString() {
        return "SubThread{" +
                "_key='" + _key + '\'' +
                ", parentThread='" + parentThread + '\'' +
                ", title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", content='" + content + '\'' +
                ", hasImage=" + hasImage +
                ", time='" + time + '\'' +
                '}';
    }
}

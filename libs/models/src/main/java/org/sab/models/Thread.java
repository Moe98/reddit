package org.sab.models;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Thread {
    private String name;
    private String description;
    private String creatorId;
    private long numOfFollowers;
    private String dateCreated;

    public Thread() {
        super();
    }
    private Thread(String name, String creatorId,
                      String description) {

        this.name = name;
        this.creatorId = creatorId;
        this.description = description;
    }

    public static Thread createNewThread(String name, String creatorId,
                                               String description) {

        return new Thread(name, creatorId, description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // TODO fix duplicate methods
    public String getCreatorId() {
        return this.creatorId;
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

    public long getNumOfFollowers() {
        return this.numOfFollowers;
    }

    public void setNumOfFollowers(long numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    // TODO use enums
    public static String getCollectionName() {
        return CollectionNames.THREAD.get();
    }

    public static String getNameAttributeName() {
        return "_key";
    }

    public static String getDescriptionAttributeName() {
        return "Description";
    }

    public static String getCreatorAttributeName() {
        return "Creator";
    }

    public static String getNumOfFollowersAttributeName() {
        return "NumOfFollowers";
    }

    public static String getDateCreatedAttributeName() {
        return "DateCreated";
    }

    @Override
    public String toString() {
        return "Thread{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creator='" + creatorId + '\'' +
                ", numOfFollowers=" + numOfFollowers +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject thread = new JSONObject();
        thread.put(ThreadAttributes.THREAD_NAME.getHTTP(), name);
        thread.put(ThreadAttributes.DESCRIPTION.getHTTP(), description);
        thread.put(ThreadAttributes.CREATOR_ID.getHTTP(), creatorId);
        thread.put(ThreadAttributes.NUM_OF_FOLLOWERS.getHTTP(), numOfFollowers);
        thread.put(ThreadAttributes.DATE_CREATED.getHTTP(), dateCreated);
        return thread;
    }
}

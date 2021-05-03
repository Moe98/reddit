package org.sab.models;

import org.json.JSONObject;

import java.util.Date;

@SuppressWarnings("unused")
public class Thread {
    private String name;
    private String description;
    // TODO change |creator| to be final, as the creator of the thread
    //  cannot change.
    private String creatorId;
    private long numOfFollowers;
    private String dateCreated;

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setNumOfFollowers(long numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCreatorId() { return this.creatorId; }

    public long getNumOfFollowers() {
        return this.numOfFollowers;
    }

    public String getDateCreated() {
        return this.dateCreated;
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

    public JSONObject toJSON(){
        JSONObject thread = new JSONObject();
        thread.put("name",name);
        thread.put("description",description);
        thread.put("creatorId",creatorId);
        thread.put("numOfFollowers",numOfFollowers);
        thread.put("dateCreated",dateCreated);
        return thread;
    }
}

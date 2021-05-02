package org.sab.models;

@SuppressWarnings("unused")
public class Thread {
    public String name;
    public String description;
    // TODO change |creator| to be final, as the creator of the thread
    //  cannot change.
    public String creator;
    public long numOfFollowers;
    public String dateCreated;

    public Thread() {
        // Empty constructor used in the recommendation app.
        super();
    }

    public Thread(String name, String description, String creator, long numOfFollowers, String dateCreated) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.numOfFollowers = numOfFollowers;
        this.dateCreated = dateCreated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setNumOfFollowers(long numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "Thread{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creator='" + creator + '\'' +
                ", numOfFollowers=" + numOfFollowers +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }
}

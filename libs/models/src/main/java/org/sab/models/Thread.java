package org.sab.models;

@SuppressWarnings("unused")
public class Thread {
    public String name;
    public String description;
    public String creator;
    public long numOfFollowers;
    public String dateCreated;

    public Thread() {
        super();
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

    public static String getCollectionName() {
        return "Threads";
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
                ", creator='" + creator + '\'' +
                ", numOfFollowers=" + numOfFollowers +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }
}

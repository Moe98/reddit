package org.sab.models;

@SuppressWarnings("unused")
public class Thread {
    String name;
    String description;
    String creator;
    long numOfFollowers;
    String dateCreated;

    public Thread() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getNumOfFollowers() {
        return numOfFollowers;
    }

    public void setNumOfFollowers(long numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public String getDateCreated() {
        return dateCreated;
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

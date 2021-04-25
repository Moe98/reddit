package org.sab.recommendation;

public class Thread {
    public String name;
    public String description;
    public String creator;
    public int numOfFollowers;
    public String dateCreated;

    Thread(){
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

    public void setNumOfFollowers(int numOfFollowers) {
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

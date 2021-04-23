package org.sab.chat.storage.models;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(keyspace = "chat_app", name = "chats")
public class Chat {

    @PartitionKey
    private UUID chat_id;
    private String name;
    private String description;
    private List<java.util.UUID> members;
    private UUID admin;
    private Date date_created;

    public Chat() {
    }

    public Chat(UUID chat_id, String name, String description, List<UUID> members, UUID admin) {
        this.chat_id = chat_id;
        this.name = name;
        this.description = description;
        this.members = members;
        this.admin = admin;
        this.date_created = new Date();
    }

    public UUID getChat_id() {
        return chat_id;
    }

    public void setChat_id(UUID chat_id) {
        this.chat_id = chat_id;
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

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    public UUID getAdmin() {
        return admin;
    }

    public void setAdmin(UUID admin) {
        this.admin = admin;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chat_id=" + chat_id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", members=" + members +
                ", admin=" + admin +
                ", date_created=" + date_created +
                '}';
    }
}

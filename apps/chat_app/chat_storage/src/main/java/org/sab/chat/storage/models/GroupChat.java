package org.sab.chat.storage.models;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(keyspace = "chat_app", name = "group_chats")
public class GroupChat {

    @PartitionKey
    private UUID chat_id;
    private String name;
    private String description;
    private List<java.util.UUID> members;
    private UUID admin;
    private Date date_created;

    public GroupChat() {
    }
    public GroupChat(UUID chat_id, String name, String description, List<UUID> members, UUID admin,Date date_created) {
        this.chat_id = chat_id;
        this.name = name;
        this.description = description;
        this.members = members;
        this.admin = admin;
        this.date_created = date_created;
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

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("chatId", chat_id.toString());
        json.put("name", name);
        json.put("description", description);
        json.put("adminId", admin.toString());
        json.put("dateCreated", date_created.toString());

        JSONArray membersList = new JSONArray();
        members.stream().map(memberId -> memberId.toString()).forEach(membersList::put);
        json.put("memberIds", membersList);

        return json;
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

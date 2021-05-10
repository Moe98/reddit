package org.sab.chat.storage.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.json.simple.JSONObject;
import org.sab.chat.storage.tables.TableUtils;

import java.util.UUID;

@Table(keyspace = "chat_app", name = "group_messages")
public class GroupMessage {

    @PartitionKey
    private UUID chat_id;
    @ClusteringColumn
    private UUID message_id;
    private UUID sender_id;
    private String content;

    public GroupMessage() {
    }

    public GroupMessage(UUID chat_id, UUID message_id, UUID sender_id, String content) {
        this.chat_id = chat_id;
        this.message_id = message_id;
        this.sender_id = sender_id;
        this.content = content;
    }

    public UUID getChat_id() {
        return chat_id;
    }

    public void setChat_id(UUID chat_id) {
        this.chat_id = chat_id;
    }

    public UUID getMessage_id() {
        return message_id;
    }

    public void setMessage_id(UUID message_id) {
        this.message_id = message_id;
    }

    public UUID getSender_id() {
        return sender_id;
    }

    public void setSender_id(UUID sender_id) {
        this.sender_id = sender_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("chatId", chat_id.toString());
        json.put("timestamp", TableUtils.getInstantFromUUID(message_id));
        json.put("senderId", sender_id.toString());
        json.put("content", content);
        return json;
    }

    @Override
    public String toString() {
        return "Message{" +
                "chat_id=" + chat_id +
                ", message_id=" + message_id +
                ", sender_id=" + sender_id +
                ", content='" + content + '\'' +
                '}';
    }

}

package org.sab.chat.storage.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "chat_app", name = "direct_messages")
public class DirectMessage {

    @PartitionKey
    private UUID chat_id;
    @ClusteringColumn
    private UUID message_id;
    private UUID sender_id;
    private String content;

    public DirectMessage() {
    }

    public DirectMessage(UUID chat_id, UUID message_id, UUID sender_id, String content) {
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


    @Override
    public String toString() {
        return "DirectMessage{" +
                "chat_id=" + chat_id +
                ", message_id=" + message_id +
                ", sender_id=" + sender_id +
                ", content='" + content + '\'' +
                '}';
    }

}

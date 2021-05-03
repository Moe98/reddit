package org.sab.chat.storage.models;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "chat_app", name = "direct_chats")
public class DirectChat {

    @PartitionKey
    private UUID chat_id;
    private UUID first_member;
    private UUID second_member;

    public DirectChat() {
    }

    public DirectChat(UUID chat_id, UUID first_member, UUID second_member) {
        this.chat_id = chat_id;
        this.first_member = first_member;
        this.second_member = second_member;
    }

    public UUID getChat_id() {
        return chat_id;
    }

    public void setChat_id(UUID chat_id) {
        this.chat_id = chat_id;
    }

    public UUID getFirst_member() {
        return first_member;
    }

    public void setFirst_member(UUID first_member) {
        this.first_member = first_member;
    }

    public UUID getSecond_member() {
        return second_member;
    }

    public void setSecond_member(UUID second_member) {
        this.second_member = second_member;
    }

    @Override
    public String toString() {
        return "DirectChat{" +
                "chat_id=" + chat_id +
                ", first_member=" + first_member +
                ", second_member=" + second_member +
                '}';
    }
}

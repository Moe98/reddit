package org.sab.chat.server.models;

import com.sun.tools.javac.Main;
import io.netty.channel.ChannelId;

import java.util.ArrayList;
import java.util.UUID;

public class Client {
    ChannelId channelId;
    ArrayList<UUID> chatIds;
    public Client(ChannelId id){
        this.channelId = id;
        this.chatIds = new ArrayList<>();
    }

    public ChannelId getId() {
        return channelId;
    }


    public ArrayList<UUID> getChatIds() {
        return chatIds;
    }

    public void setChatIds(ArrayList<UUID> chatIds) {
        this.chatIds = chatIds;
    }
    @Override
    public String toString() {
        return "Client{" +
                "id=" + channelId +
                ", chatIds=" + chatIds +
                '}';
    }

}

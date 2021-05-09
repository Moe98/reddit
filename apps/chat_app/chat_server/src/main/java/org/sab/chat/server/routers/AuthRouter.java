package org.sab.chat.server.routers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.ChatServer;
import org.sab.chat.server.models.ClientManager;

import java.util.ArrayList;
import java.util.UUID;

public class AuthRouter extends Router {
    @Override
    public void forwardToQueue(ChannelHandlerContext ctx, JSONObject request) {
        System.out.println("Forward to queue");
    }

    @Override
    public void route(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("Auth");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        randomChatIds.add(UUID.fromString("ee55dcf8-ee7b-429a-939e-12c2f7b7ddee"));
        UUID userIdFromDataBase = randomChatIds.get(ChatServer.couter++);
        ArrayList<Channel> channels = ClientManager.activeUsers.getOrDefault(userIdFromDataBase, new ArrayList<>());
        channels.add(ctx.channel());
        System.out.println("User id: " + userIdFromDataBase + " User channels: " + channels.toString());

        ClientManager.activeUsers.put(userIdFromDataBase, channels);
        ClientManager.channelToUser.put(ctx.channel(),userIdFromDataBase);
    }
}

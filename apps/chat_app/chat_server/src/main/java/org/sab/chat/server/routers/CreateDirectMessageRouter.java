package org.sab.chat.server.routers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CreateDirectMessageRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        String[] attributes = {"chatId", "senderId", "content"};
        packAndForwardRequest(ctx, request, attributes);
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("CreateDirectMessage");
        String content = (String)response.get("content");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        ArrayList<UUID> members = randomChatIds;
        TextWebSocketFrame message;
        for (UUID memberId : members) {
            if (ClientManager.isUserOnline(memberId)) {
                ConcurrentLinkedQueue<Channel> memberChannels = ClientManager.getUserChannels(memberId);
                for (Channel channel : memberChannels) {
                    message = new TextWebSocketFrame(content);
                    channel.writeAndFlush(message.retain());
                }
            } else {
                //notification
            }
        }
    }
}

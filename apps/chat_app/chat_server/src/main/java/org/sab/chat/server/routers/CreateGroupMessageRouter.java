package org.sab.chat.server.routers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CreateGroupMessageRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        String[] attributes = {"chatId", "senderId", "content"};
        packAndForwardRequest(ctx, request, attributes);
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("CreateGroupMessage");
        String chatId = (String)(((JSONObject)response.get("data")).get("chatId"));
        ConcurrentLinkedQueue<UUID> members = ClientManager.getChatMembers(UUID.fromString(chatId));
        String content = (String)(((JSONObject)response.get("data")).get("content"));

        TextWebSocketFrame message;
        for (UUID memberId : members) {
            if (ClientManager.isUserOnline(memberId)) {
                ConcurrentLinkedQueue<Channel> memberChannels = ClientManager.getUserChannels(memberId);
                for (Channel channel : memberChannels) {
                    message = new TextWebSocketFrame(content);
                    channel.writeAndFlush(message.retain());
                }
            }else {
                //notification
            }
        }
    }
}

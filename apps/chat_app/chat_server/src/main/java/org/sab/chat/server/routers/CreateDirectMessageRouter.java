package org.sab.chat.server.routers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.UUID;

public class CreateDirectMessageRouter extends Router {
    @Override
    public void forwardToQueue(ChannelHandlerContext ctx, JSONObject request) {
        JSONObject body = new JSONObject();
        body.put("chatId", request.get("chatId"));
        body.put("senderId", request.get("senderId"));
        body.put("content", request.get("content"));

        String functionName = (String) request.get("type");
        JSONObject packedRequest = packRequest(functionName, body);

        ctx.fireChannelRead(packedRequest);
    }

    @Override
    public void route(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("CreateDirectMessage");
        String content = (String)response.get("content");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        ArrayList<UUID> members = randomChatIds;
        TextWebSocketFrame message;
        for (int i = 0; i < members.size(); i++) {
            UUID memberID = members.get(i);
            if (ClientManager.activeUsers.containsKey(memberID)) {
                ArrayList<Channel> memberChannels = ClientManager.activeUsers.get(memberID);
                for (int j = 0; j < memberChannels.size(); j++) {
                    message = new TextWebSocketFrame(content);
                    memberChannels.get(j).writeAndFlush(message.retain());
                }
            }else{
                //notification
            }
        }
    }
}

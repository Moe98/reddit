package org.sab.chat.server.routers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sab.chat.server.ChatServer;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.UUID;

public class CreateGroupMessageRouter extends Router {
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
        System.out.println("CreateGroupMessage");
        String content = (String) (((JSONObject) response.get("data")).get("content"));

        JSONArray members = (JSONArray) (((JSONObject) response.get("data")).get("memberIds"));

//        TextWebSocketFrame message;
//        for (int i = 0; i < members.size(); i++) {
//            UUID memberID = UUID. fromString((String)members.get(i));
//            if (ClientManager.activeUsers.containsKey(memberID)) {
//                ArrayList<Channel> memberChannels = ClientManager.activeUsers.get(memberID);
//                for (int j = 0; j < memberChannels.size(); j++) {
//                    message = new TextWebSocketFrame(content);
//                    memberChannels.get(j).writeAndFlush(message.retain());
//                }
//            }
//        }
    }
}

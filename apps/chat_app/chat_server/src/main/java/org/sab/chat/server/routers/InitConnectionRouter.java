package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class InitConnectionRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        // Should authenticate before this
        UUID userId = UUID.fromString((String) request.get("userId"));
        ClientManager.handleUserOnline(userId, ctx.channel());

        // Get user chats
        JSONObject body = new JSONObject();
        body.put("userId", userId.toString());

        String functionName = (String) request.get("type");
        JSONObject packedRequest = packRequest(functionName, body);

        ctx.fireChannelRead(packedRequest);
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        if (response.get("data") == null) {
            ctx.writeAndFlush(response.clone());
            return;
        }

        UUID userId = ClientManager.getChannelUser(ctx.channel().id());
        JSONObject data = (JSONObject) response.get("data");

        HashMap<UUID, List<UUID>> chats = new HashMap<>();
        JSONArray directChatsJson = (JSONArray) data.get("directChats");
        JSONArray groupChatsJson = (JSONArray) data.get("groupChats");

        for (int i = 0; i < directChatsJson.length(); i++) {
            JSONObject chat = (JSONObject) directChatsJson.get(i);
            UUID chatId = UUID.fromString((String) chat.get("chatId"));
            List<UUID> chatMembers = new ArrayList<>();
            chatMembers.add(UUID.fromString((String) chat.get("firstMember")));
            chatMembers.add(UUID.fromString((String) chat.get("secondMember")));
            chats.put(chatId, chatMembers);
        }

        for (int i = 0; i < groupChatsJson.length(); i++) {
            JSONObject chat = (JSONObject) groupChatsJson.get(i);
            UUID chatId = UUID.fromString((String) chat.get("chatId"));

            JSONArray membersIds = (JSONArray) chat.get("membersIds");
            List<UUID> chatMembers = new ArrayList<>();
            for (int j = 0; j < membersIds.length(); i++)
                chatMembers.add(UUID.fromString((String) membersIds.get(j)));
            chats.put(chatId, chatMembers);
        }

        ClientManager.syncUserChats(userId, chats);

        ctx.writeAndFlush(response.clone());
    }
}

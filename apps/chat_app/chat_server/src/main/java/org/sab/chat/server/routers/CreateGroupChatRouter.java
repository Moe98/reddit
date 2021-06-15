package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateGroupChatRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        boolean isAuthenticated = authenticate(request, "creator");
        if (!isAuthenticated) {
            rejectUnAuthenticatedRequest(ctx);
            return;
        }
        String[] attributes = {"creator", "name", "description"};
        packAndForwardRequest(ctx, request, attributes);
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        JSONObject data = (JSONObject) response.get("data");
        if(data == null) {
            handleError(ctx, response);
            return;
        }

        UUID chatId = UUID.fromString((String) data.get("chatId"));

        List<UUID> memberIds = new ArrayList<>();
        memberIds.add(UUID.fromString((String) data.get("adminId")));

        ClientManager.handleUserCreateChat(chatId, memberIds);

        ClientManager.broadcastResponseToChatChannels(chatId, response);
    }
}

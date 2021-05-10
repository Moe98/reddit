package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.UUID;

public class LeaveGroupRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        String[] attributes = {"chatId", "userId"};
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
        UUID targetUserId = UUID.fromString((String) data.get("targetUserId"));
        UUID adminId = UUID.fromString((String) data.get("adminId"));

        boolean isAdmin = targetUserId.equals(adminId);
        ClientManager.handleUserLeftGroup(chatId, targetUserId, isAdmin);

        System.out.println("Route response to client");
    }
}

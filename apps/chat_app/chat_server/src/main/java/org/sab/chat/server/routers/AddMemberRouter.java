package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.UUID;

public class AddMemberRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        String[] attributes = {"chatId", "adminId", "memberId"};
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
        ClientManager.handleMemberAdded(chatId, targetUserId);

        System.out.println("Route response to client");
    }
}

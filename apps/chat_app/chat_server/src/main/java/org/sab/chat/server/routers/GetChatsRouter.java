package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

import java.util.UUID;

public class GetChatsRouter extends Router {

    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        boolean isAuthenticated = authenticate(request, "userId");
        if (!isAuthenticated) {
            rejectUnAuthenticatedRequest(ctx);
            return;
        }
        String[] attributes = {"userId"};
        packAndForwardRequest(ctx, request, attributes);
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        JSONObject data = (JSONObject) response.get("data");
        if(data == null) {
            handleError(ctx, response);
            return;
        }

        ClientManager.sendResponseToChannel(ctx.channel(), response);
    }
}

package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class GetGroupMessagesRouter extends Router {

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

        ctx.writeAndFlush(response.clone());
    }
}

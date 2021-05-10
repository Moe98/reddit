package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class RemoveMemberRouter extends Router {
    @Override
    public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request) {
        System.out.println("Forward to queue");
    }

    @Override
    public void routeResponse(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("Route response");
    }
}

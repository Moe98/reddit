package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class CreateDirectChatRouter extends Router {
    @Override
    public void forwardToQueue(ChannelHandlerContext ctx, JSONObject request) {
        JSONObject body = new JSONObject();
        body.put("firstMember", request.get("firstMember"));
        body.put("secondMember", request.get("secondMember"));

        String functionName = (String) request.get("type");
        JSONObject packedRequest = packRequest(functionName, body);

        ctx.fireChannelRead(packedRequest);
    }

    @Override
    public void route(ChannelHandlerContext ctx, JSONObject response) {
        System.out.println("Route response");
    }
}

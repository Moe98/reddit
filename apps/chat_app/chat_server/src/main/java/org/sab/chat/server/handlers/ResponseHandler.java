package org.sab.chat.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

public class ResponseHandler extends SimpleChannelInboundHandler<JSONObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JSONObject queueResponse) {
        queueResponse.remove("statusCode");
        ClientManager.routeResponse((JSONObject) queueResponse.clone(), ctx);
    }
}

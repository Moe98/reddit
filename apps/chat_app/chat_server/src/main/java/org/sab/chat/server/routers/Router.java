package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public abstract class Router {
    abstract public void forwardToQueue(ChannelHandlerContext ctx, JSONObject request);

    abstract public void route(ChannelHandlerContext ctx, JSONObject response);
}

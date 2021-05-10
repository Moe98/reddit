package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import org.json.simple.JSONObject;

public abstract class Router {

    public JSONObject packRequest(String functionName, JSONObject body) {
        JSONObject packedRequest = new JSONObject();
        packedRequest.put("body", body);
        packedRequest.put("uriParams", new JSONObject());
        packedRequest.put("methodType", HttpMethod.POST.toString());
        packedRequest.put("headers", new JSONObject());
        packedRequest.put("functionName", functionName);
        return packedRequest;
    }

    abstract public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request);

    abstract public void routeResponse(ChannelHandlerContext ctx, JSONObject response);
}

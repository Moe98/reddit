package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import org.json.simple.JSONObject;
import org.sab.chat.server.ClientManager;

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

    public void packAndForwardRequest(ChannelHandlerContext ctx, JSONObject request, String... attributes) {
        JSONObject body = new JSONObject();
        for(String attribute : attributes)
            body.put(attribute, request.get(attribute));
        String functionName = (String) request.get("type");
        JSONObject packedRequest = packRequest(functionName, body);
        ctx.fireChannelRead(packedRequest);
    }

    public void handleError(ChannelHandlerContext ctx, JSONObject response) {
        response.put("type", "ERROR");
        ClientManager.sendResponseToChannel(ctx.channel(), response);
    }

    abstract public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request);

    abstract public void routeResponse(ChannelHandlerContext ctx, JSONObject response);
}
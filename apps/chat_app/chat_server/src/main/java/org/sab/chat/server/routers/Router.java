package org.sab.chat.server.routers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import org.json.simple.JSONObject;
import org.sab.auth.AuthParamsHandler;
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

    protected boolean authenticate(JSONObject request, String userKey) {
        String authToken = (String) request.get("authToken");
        org.json.JSONObject authenticationParams = AuthParamsHandler.decodeToken(authToken);
        boolean isAuthenticated = authenticationParams.getBoolean(AuthParamsHandler.IS_AUTHENTICATED);
        if(!isAuthenticated) return false;
        String userId = (String) authenticationParams.get("userId");
        request.put(userKey, userId);
        return true;
    }

    protected void rejectUnAuthenticatedRequest(ChannelHandlerContext ctx) {
        JSONObject response = new JSONObject();
        response.put("type", "ERROR");
        response.put("msg", "Unauthorised request");
        ctx.channel().writeAndFlush(response);
    }

    abstract public void forwardRequestToQueue(ChannelHandlerContext ctx, JSONObject request);

    abstract public void routeResponse(ChannelHandlerContext ctx, JSONObject response);
}

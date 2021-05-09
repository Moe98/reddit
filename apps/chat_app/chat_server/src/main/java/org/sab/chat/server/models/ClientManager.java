package org.sab.chat.server.models;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.sab.chat.server.routers.Router;
import org.sab.chat.server.routers.RouterBuilder;

import java.util.*;

public class ClientManager {
    public static final HashMap<UUID, ArrayList<Channel>> activeUsers = new HashMap<>();
    public static final HashMap<Channel, UUID> channelToUser = new HashMap<>();

    public static void routeRequest(JSONObject messageJson, ChannelHandlerContext ctx) {
        String type = (String) messageJson.get("type");
        Router router = RouterBuilder.createRouter(type);
        if(router == null) {
            handleNonsupportedType(ctx);
            return;
        }
        router.forwardToQueue(ctx, messageJson);
        router.route(ctx, messageJson);
    }

    public static void handleNonsupportedType(ChannelHandlerContext ctx){
        System.out.println("non supported type");
    }

}

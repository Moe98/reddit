package org.sab.chat.server.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.chat.server.models.ClientManager;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class TextWebSocketFrameHandler extends
        SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,
                                   Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            group.writeAndFlush(new TextWebSocketFrame("Client " +
                    ctx.channel() + " joined"));
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UUID user = ClientManager.channelToUser.remove(ctx.channel());
        ArrayList<Channel> userChannels = ClientManager.activeUsers.get(user);
        userChannels.remove(ctx.channel());
        if(userChannels.size()==0)
            ClientManager.activeUsers.remove(user);
        else{
            ClientManager.activeUsers.put(user,userChannels);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws ParseException {
        msg.retain();
        JSONParser parser = new JSONParser();
        JSONObject messageJson = (JSONObject) parser.parse(msg.text());
        ClientManager.routeRequest(messageJson, ctx);
    }

}
package org.sab.chat.server.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.chat.server.models.ClientManager;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.sab.chat.server.ChatServer.clients;

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
//            ArrayList<UUID> chatIds = new ArrayList<>();
//            Collections.shuffle(randomChatIds);
//            chatIds.add(randomChatIds.get(0));
//            chatIds.add(randomChatIds.get(1));
//            clients.put(ctx.channel().id(), chatIds);
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws ParseException {
        msg.retain();
        JSONParser parser = new JSONParser();
        JSONObject messageJson = (JSONObject) parser.parse(msg.text());
        ClientManager.routeRequest(messageJson, ctx);
//        TextWebSocketFrame message = new TextWebSocketFrame((String) messageJson.get("message"));
//        group.writeAndFlush(message.retain(), new ChannelMatcher() {
//            @Override
//            public boolean matches(Channel channel) {
//                System.out.println(channel.id());
//                ArrayList<UUID> memberChats = clients.get(channel.id());
//                return memberChats.contains(chatId);
//            }
//        });
    }

}
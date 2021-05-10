package org.sab.chat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;
import org.sab.chat.server.routers.Router;
import org.sab.chat.server.routers.RouterBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientManager {
    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Channel>> activeUsers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ChannelId, UUID> channelToUser = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<UUID>> chatMembers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<UUID>> userChats = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Boolean>> chatsRefCount = new ConcurrentHashMap<>();

    public static ConcurrentLinkedQueue<Channel> getUserChannels(UUID userId) {
        return activeUsers.get(userId);
    }

    public static ConcurrentLinkedQueue<UUID> getUserChats(UUID userId) {
        return userChats.get(userId);
    }

    public static ConcurrentLinkedQueue<UUID> getChatMembers(UUID chatId) {
        return chatMembers.get(chatId);
    }

    public static UUID getChannelUser(ChannelId channelId) {
        return channelToUser.get(channelId);
    }

    public static boolean isUserOnline(UUID userId) {
        return activeUsers.containsKey(userId);
    }

    public static void handleUserOnline(UUID userId, Channel channel) {
        activeUsers.putIfAbsent(userId, new ConcurrentLinkedQueue<>());
        ClientManager.getUserChannels(userId).add(channel);
        channelToUser.putIfAbsent(channel.id(), userId);
    }

    public static void handleUserOffline(UUID userId, Channel channel) {
        channelToUser.remove(channel.id());
        ConcurrentLinkedQueue<UUID> userChats = getUserChats(userId);

        List<UUID> chatsToDelete = new ArrayList<>();
        for (UUID chatId : userChats) {
            ConcurrentLinkedQueue<Boolean> refCount = chatsRefCount.get(chatId);
            refCount.remove(true);
            if (refCount.isEmpty())
                chatsToDelete.add(chatId);
        }

        for (UUID chatId : chatsToDelete) {
            chatMembers.remove(chatId);
            chatsRefCount.remove(chatId);
        }

        ConcurrentLinkedQueue<Channel> userChannels = getUserChannels(userId);
        userChannels.remove(channel);
        if (userChannels.isEmpty()) {
            activeUsers.remove(userId);
            userChats.remove(userId);
        }
    }

    public static void syncUserChats(UUID userId, HashMap<UUID, List<UUID>> chats) {
        userChats.putIfAbsent(userId, new ConcurrentLinkedQueue<>(chats.keySet()));
        for (Map.Entry<UUID, List<UUID>> chat : chats.entrySet()) {
            UUID chatId = chat.getKey();
            chatMembers.putIfAbsent(chatId, new ConcurrentLinkedQueue<>(chat.getValue()));
            chatsRefCount.putIfAbsent(chatId, new ConcurrentLinkedQueue<>());
            chatsRefCount.get(chatId).add(true);
        }
    }

    public static void handleMemberAdded(UUID chatId, UUID memberId) {
        chatMembers.get(chatId).add(memberId);
        userChats.get(memberId).add(chatId);
    }

    public static void handleMemberRemoved(UUID chatId, UUID memberId) {
        chatMembers.get(chatId).remove(memberId);
        userChats.get(memberId).remove(chatId);
    }

    public static void handleUserLeftGroup(UUID chatId, UUID userId, boolean isAdmin) {
        if (isAdmin) {
            ConcurrentLinkedQueue<UUID> chatMemberIds = getChatMembers(chatId);
            for (UUID memberId : chatMemberIds)
                userChats.get(memberId).remove(chatId);
            chatMembers.remove(chatId);
        } else {
            handleMemberRemoved(chatId, userId);
        }
    }

    public static void handleUserCreateChat(UUID chatId, List<UUID> memberIds) {
        chatMembers.putIfAbsent(chatId, new ConcurrentLinkedQueue<>(memberIds));
        for (UUID memberId : memberIds)
            userChats.get(memberId).add(chatId);
    }

    public static void broadcastResponseToChatChannels(UUID chatId, JSONObject response) {
        TextWebSocketFrame message = new TextWebSocketFrame(response.toString());
        ConcurrentLinkedQueue<UUID> memberIds = ClientManager.getChatMembers(chatId);
        for (UUID memberId : memberIds) {
            if (isUserOnline(memberId)) {
                ConcurrentLinkedQueue<Channel> memberChannels = ClientManager.getUserChannels(memberId);
                for (Channel channel : memberChannels) {
                    channel.writeAndFlush(message);
                }
            } else {
                // Notify
            }
        }
    }

    public static void forwardRequestToQueue(JSONObject messageJson, ChannelHandlerContext ctx) {
        String type = (String) messageJson.get("type");
        Router router = RouterBuilder.createRouter(type);
        if (router == null) {
            handleNonSupportedType(ctx);
            return;
        }
        router.forwardRequestToQueue(ctx, messageJson);
    }

    public static void routeResponse(JSONObject messageJson, ChannelHandlerContext ctx) {
        String type = (String) messageJson.get("type");
        Router router = RouterBuilder.createRouter(type);
        router.routeResponse(ctx, messageJson);
    }

    public static void handleNonSupportedType(ChannelHandlerContext ctx) {
        JSONObject response = new JSONObject();
        response.put("type", "ERROR");
        response.put("msg", "Non Supported Message Type");
        ctx.channel().writeAndFlush(response);
    }

}

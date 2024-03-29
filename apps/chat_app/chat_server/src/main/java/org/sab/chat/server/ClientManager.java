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

import static org.sab.innerAppComm.Comm.notifyApp;

public class ClientManager {
    private static final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Channel>> activeUsers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ChannelId, UUID> channelToUser = new ConcurrentHashMap<>();
    public static final String Notification_Queue_Name = "NOTIFICATION_REQ";
    protected static final String SEND_NOTIFICATION_FUNCTION_NAME = "SEND_NOTIFICATION";

    public static ConcurrentHashMap<UUID, String> userIdToUsername = new ConcurrentHashMap<>();

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
        if (isUserOnline(memberId))
            chatsRefCount.get(chatId).add(true);
        if (userChats.containsKey(memberId))
            userChats.get(memberId).add(chatId);
    }

    public static void handleMemberRemoved(UUID chatId, UUID memberId) {
        chatMembers.get(chatId).remove(memberId);
        if (isUserOnline(memberId))
            chatsRefCount.get(chatId).remove(true);
        if (userChats.containsKey(memberId))
            userChats.get(memberId).remove(chatId);
    }

    public static void handleUserLeftGroup(UUID chatId, UUID userId, boolean isAdmin) {
        if (isAdmin) {
            ConcurrentLinkedQueue<UUID> chatMemberIds = getChatMembers(chatId);
            for (UUID memberId : chatMemberIds)
                if (userChats.containsKey(memberId))
                    userChats.get(memberId).remove(chatId);
            chatMembers.remove(chatId);
            chatsRefCount.remove(chatId);
        } else {
            handleMemberRemoved(chatId, userId);
        }
    }

    public static void handleUserCreateChat(UUID chatId, List<UUID> memberIds) {
        chatMembers.putIfAbsent(chatId, new ConcurrentLinkedQueue<>(memberIds));
        chatsRefCount.putIfAbsent(chatId, new ConcurrentLinkedQueue<>());
        for (UUID memberId : memberIds) {
            if (userChats.containsKey(memberId))
                userChats.get(memberId).add(chatId);
            if (isUserOnline(memberId))
                chatsRefCount.get(chatId).add(true);
        }

    }

    public static void sendResponseToChannel(Channel channel, JSONObject response) {
        TextWebSocketFrame message = new TextWebSocketFrame(response.toString());
        channel.writeAndFlush(message);
    }

    public static void sendResponseToUser(UUID userId, JSONObject response) {
        String responseString = response.toString();
        for (Channel channel : getUserChannels(userId))
            channel.writeAndFlush(new TextWebSocketFrame(responseString));
    }

    public static void broadcastResponseToChatChannels(UUID chatId, JSONObject response) {
        ConcurrentLinkedQueue<UUID> memberIds = ClientManager.getChatMembers(chatId);
        for (UUID memberId : memberIds) {
            if (isUserOnline(memberId)) {
                sendResponseToUser(memberId, response);
            } else {
                String username = userIdToUsername.get(memberId);
                notifyApp(Notification_Queue_Name, "`From chats:" + response.toString(), chatId.toString(), username, SEND_NOTIFICATION_FUNCTION_NAME);
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

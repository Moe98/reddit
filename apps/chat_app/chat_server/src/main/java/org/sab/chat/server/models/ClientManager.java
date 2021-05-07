package org.sab.chat.server.models;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.simple.JSONObject;
import org.sab.chat.server.ChatServer;

import java.util.*;

public class ClientManager {
    public HashMap<UUID, ArrayList<Channel>> activeUsers;
    public HashMap<Channel, UUID> channelToUser;

    public ClientManager() {
        this.activeUsers = new HashMap<>();
        this.channelToUser = new HashMap<>();
    }

    public static void routeRequest(JSONObject messageJson, ChannelHandlerContext ctx) {
        String type = (String) messageJson.get("type");
        switch (type) {
            case "Auth":
                authenticate((String) messageJson.get("userName"), ctx);
                break;
            case "AddGroupMember":
                addGroupMember((String) messageJson.get("admin"), (String) messageJson.get("chatId"), (String) messageJson.get("user"));
                break;
            case "RemoveGroupMember":
                removeGroupMember((String) messageJson.get("admin"), (String) messageJson.get("chatId"), (String) messageJson.get("user"));
                break;
            case "LeaveChat":
                leaveChat((String) messageJson.get("chatId"), (String) messageJson.get("user"));
                break;
            case "GetDirectMessages":
                getDirectMessages((String) messageJson.get("chatId"), (String) messageJson.get("user"));
                break;
            case "GetGroupMessages":
                getGroupMessages((String) messageJson.get("chatId"), (String) messageJson.get("user"));
                break;
            case "CreateGroupMessage":
                createGroupMessage((String) messageJson.get("chatId"), (String) messageJson.get("sender_id"), (String) messageJson.get("content"));
                break;
            case "CreateGroupChat":
                createGroupChat((String) messageJson.get("creator"), (String) messageJson.get("name"), (String) messageJson.get("description"));
                break;
            case "CreateDirectChat":
                createDirectChat((String) messageJson.get("first_member"), (String) messageJson.get("second_member"));
                break;
            default: //CreateDirectMessage
                createDirectMessage((String) messageJson.get("chatId"), (String) messageJson.get("sender_id"), (String) messageJson.get("content"));
                break;
        }
    }

    public static void authenticate(String userName, ChannelHandlerContext ctx) {
        System.out.println("Auth");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        randomChatIds.add(UUID.fromString("ee55dcf8-ee7b-429a-939e-12c2f7b7ddee"));
        UUID userIdFromDataBase = randomChatIds.get(ChatServer.couter++);
        ArrayList<Channel> channels = ChatServer.clients.activeUsers.getOrDefault(userIdFromDataBase, new ArrayList<>());
        channels.add(ctx.channel());
        System.out.println("User id: " + userIdFromDataBase + " User channels: " + channels.toString());

        ChatServer.clients.activeUsers.put(userIdFromDataBase, channels);
        ChatServer.clients.channelToUser.put(ctx.channel(),userIdFromDataBase);
    }

    public static void addGroupMember(String admin, String chatId, String user) {
        System.out.println("AddGroupMember");
    }

    public static void removeGroupMember(String admin, String chatId, String user) {
        System.out.println("RemoveGroupMember");
    }

    public static void leaveChat(String chatId, String user) {
        System.out.println("LeaveChat");
    }

    public static void getDirectMessages(String chatId, String user) {
        System.out.println("GetDirectMessages");
    }

    public static void getGroupMessages(String chatId, String user) {
        System.out.println("GetGroupMessages");
    }

    public static void createGroupMessage(String chatId, String sender_id, String content) {
        System.out.println("CreateGroupMessage");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        ArrayList<UUID> members = randomChatIds;
        TextWebSocketFrame message;
        for (int i = 0; i < members.size(); i++) {
            UUID memberID = members.get(i);
            if (ChatServer.clients.activeUsers.containsKey(memberID)) {
                ArrayList<Channel> memberChannels = ChatServer.clients.activeUsers.get(memberID);
                for (int j = 0; j < memberChannels.size(); j++) {
                    message = new TextWebSocketFrame(content);
                    memberChannels.get(j).writeAndFlush(message.retain());
                }
            }
        }

    }

    public static void createGroupChat(String creator, String name, String description) {
        System.out.println("CreateGroupChat");
    }

    public static void createDirectChat(String first_member, String second_member) {
        System.out.println("CreateDirectChat");

    }

    public static void createDirectMessage(String chatId, String sender_id, String content) {
        System.out.println("CreateDirectMessage");
        //hard coded values to be replaced by database values
        ArrayList<UUID> randomChatIds = new ArrayList<>();
        randomChatIds.add(UUID.fromString("efb3c541-9ddb-44d6-aa47-e6f2579ea177"));
        randomChatIds.add(UUID.fromString("02d0b9a2-ed84-4f1e-a86a-58aac9aec88d"));
        ArrayList<UUID> members = randomChatIds;
        TextWebSocketFrame message;
        for (int i = 0; i < members.size(); i++) {
            UUID memberID = members.get(i);
            if (ChatServer.clients.activeUsers.containsKey(memberID)) {
                ArrayList<Channel> memberChannels = ChatServer.clients.activeUsers.get(memberID);
                for (int j = 0; j < memberChannels.size(); j++) {
                    message = new TextWebSocketFrame(content);
                    memberChannels.get(j).writeAndFlush(message.retain());
                }
            }else{
                //notification
            }
        }

    }

}

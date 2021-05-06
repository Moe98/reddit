package org.sab.chat.server.models;

import com.sun.tools.javac.Main;
import io.netty.channel.ChannelId;

import java.util.ArrayList;
import java.util.UUID;

public class ClientManager {

    public ClientManager(){

    }

    public static void authenticate(String userName) {
        System.out.println("Auth");
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
    }

    public static void createGroupChat(String creator, String name, String description) {
        System.out.println("CreateGroupChat");
    }

    public static void createDirectChat(String first_member, String second_member) {
        System.out.println("CreateDirectChat");
    }

    public static void createDirectMessage(String chatId, String sender_id, String content) {
        System.out.println("CreateDirectMessage");

    }
}

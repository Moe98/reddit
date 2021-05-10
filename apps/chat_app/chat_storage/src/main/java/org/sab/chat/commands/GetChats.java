package org.sab.chat.commands;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.tables.DirectChatTable;
import org.sab.chat.storage.tables.GroupChatTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class GetChats extends CommandWithVerification {

    private DirectChatTable directChatTable;
    private GroupChatTable groupChatTable;

    public void getGroupChatTableInstance() {
        if (groupChatTable == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            groupChatTable = new GroupChatTable(cassandra);
            groupChatTable.createTable();
        }
    }

    public void getDirectChatTableInstance() {
        if (directChatTable == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            directChatTable = new DirectChatTable(cassandra);
            directChatTable.createTable();
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute chatId = new Attribute("userId", DataType.STRING, true);
        return new Schema(List.of(chatId));
    }

    @Override
    public String execute() {
        UUID userId = UUID.fromString((String) body.get("userId"));

        getDirectChatTableInstance();
        getGroupChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            List<DirectChat> directChats = directChatTable.getDirectChats(userId);
            List<GroupChat> groupChats = groupChatTable.getGroupChats(userId);

            JSONArray directChatsArray = new JSONArray();
            JSONArray groupChatsArray = new JSONArray();

            directChats.stream().map(DirectChat::toJson).forEach(directChatsArray::put);
            groupChats.stream().map(GroupChat::toJson).forEach(groupChatsArray::put);

            JSONObject responseBody = new JSONObject();
            responseBody.put("directChats", directChatsArray);
            responseBody.put("groupChats", groupChatsArray);

            response.put("statusCode", 200);
            response.put("msg", "Chats fetched successfully");
            response.put("data", responseBody);
        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}


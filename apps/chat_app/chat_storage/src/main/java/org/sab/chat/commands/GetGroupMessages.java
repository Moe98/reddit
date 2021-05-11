package org.sab.chat.commands;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupMessage;
import org.sab.chat.storage.tables.GroupMessageTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class GetGroupMessages extends CommandWithVerification {

    private GroupMessageTable groupMessageTable;

    public void getGroupMessageTableInstance() {
        if (groupMessageTable == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            groupMessageTable = new GroupMessageTable(cassandra);
            groupMessageTable.createTable();
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute chatId = new Attribute("chatId", DataType.STRING, true);
        Attribute userId = new Attribute("userId", DataType.STRING, true);

        return new Schema(List.of(chatId, userId));
    }

    @Override
    public String execute() {

        UUID chatId = UUID.fromString((String) body.get("chatId"));
        UUID userId = UUID.fromString((String) body.get("userId"));

        getGroupMessageTableInstance();

        JSONObject response = new JSONObject();
        try {
            List<GroupMessage> messages = groupMessageTable.getGroupMessages(chatId, userId);

            JSONArray messagesJson = new JSONArray();
            messages.stream().map(GroupMessage::toJson).forEach(messagesJson::put);

            JSONObject responseBody = new JSONObject();
            responseBody.put("messages", messagesJson);
            responseBody.put("chatId", chatId.toString());

            response.put("statusCode", 200);
            response.put("msg", "Chat messages fetched successfully");
            response.put("data", responseBody);

        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}


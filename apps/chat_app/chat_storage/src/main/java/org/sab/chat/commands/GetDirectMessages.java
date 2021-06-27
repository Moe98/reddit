package org.sab.chat.commands;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectMessage;
import org.sab.chat.storage.tables.DirectMessageTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class GetDirectMessages extends CommandWithVerification {

    private DirectMessageTable directMessageTable;

    public void getDirectMessageTableInstance() {
        if (directMessageTable == null) {
            CassandraConnector cassandra = CassandraConnector.getInstance();
            directMessageTable = cassandra.getDirectMessageTable();
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute chatId = new Attribute("chatId", DataType.UUID, true);
        Attribute userId = new Attribute("userId", DataType.UUID, true);

        return new Schema(List.of(chatId, userId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    public String execute() {

        UUID chatId = UUID.fromString((String) body.get("chatId"));
        UUID userId = UUID.fromString((String) body.get("userId"));

        getDirectMessageTableInstance();

        JSONObject response = new JSONObject();
        try {
            List<DirectMessage> messages = directMessageTable.getDirectMessages(chatId, userId);

            JSONArray messagesJson = new JSONArray();
            messages.stream().map(DirectMessage::toJson
            ).forEach(messagesJson::put);

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
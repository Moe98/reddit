package org.sab.chat.commands;

import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectMessage;
import org.sab.chat.storage.tables.DirectMessageTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class CreateDirectMessage extends CommandWithVerification {

    private DirectMessageTable directMessageTable;

    public void getDirectMessageTableInstance() {
        if (directMessageTable == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            directMessageTable = new DirectMessageTable(cassandra);
            directMessageTable.createTable();
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute chatId = new Attribute("chatId", DataType.STRING, true);
        Attribute senderId = new Attribute("senderId", DataType.STRING, true);
        Attribute content = new Attribute("content", DataType.STRING, true);

        return new Schema(List.of(chatId, senderId, content));
    }


    @Override
    public String execute() {

        UUID chatId = UUID.fromString((String) body.get("chatId"));
        UUID senderId = UUID.fromString((String) body.get("senderId"));
        String content = (String) body.get("content");

        getDirectMessageTableInstance();

        JSONObject response = new JSONObject();
        try {
            DirectMessage directMessage = directMessageTable.createDirectMessage(chatId, senderId, content);
            response.put("statusCode", 200);
            response.put("msg", "Direct message created successfully");
            response.put("data", directMessage.toJson());
        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}

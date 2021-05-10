package org.sab.chat.commands;

import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.models.GroupMessage;
import org.sab.chat.storage.tables.GroupMessageTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class CreateGroupMessage extends CommandWithVerification {

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
        Attribute senderId = new Attribute("senderId", DataType.STRING, true);
        Attribute content = new Attribute("content", DataType.STRING, true);

        return new Schema(List.of(chatId, senderId, content));
    }


    @Override
    public String execute() {
        UUID chatId = UUID.fromString((String) body.get("chatId"));
        UUID senderId = UUID.fromString((String) body.get("senderId"));
        String content = (String) body.get("content");

        getGroupMessageTableInstance();

        JSONObject response = new JSONObject();
        try {
            GroupChat groupChat = groupMessageTable.sendGroupMessage(chatId, senderId, content);
            response.put("statusCode", 200);
            response.put("msg", "Group message created successfully");
            response.put("data", groupChat.toJson());

        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}


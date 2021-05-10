package org.sab.chat.commands;

import org.json.simple.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.tables.GroupChatTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class LeaveGroup extends CommandWithVerification {

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

        getGroupChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            GroupChat groupChat = groupChatTable.leavesChat(chatId, userId);

            JSONObject data = groupChat.toJson();
            data.put("targetMemberId", userId.toString());

            response.put("statusCode", 200);
            response.put("msg", "User left group successfully");
            response.put("data", data);
        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}

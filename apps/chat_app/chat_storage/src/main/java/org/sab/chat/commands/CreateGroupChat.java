package org.sab.chat.commands;

import org.json.JSONObject;
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

public class CreateGroupChat extends CommandWithVerification {

    private GroupChatTable groupChatTable;

    public void getGroupChatTableInstance() {
        if (groupChatTable == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            cassandra.createTables();
            groupChatTable = cassandra.getGroupChatTable();
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute creator = new Attribute("creator", DataType.STRING, true);
        Attribute name = new Attribute("name", DataType.STRING, true);
        Attribute description = new Attribute("description", DataType.STRING, true);

        return new Schema(List.of(creator, name, description));
    }


    @Override
    public String execute() {

        UUID creator = UUID.fromString((String) body.get("creator"));
        String name = (String) body.get("name");
        String description = (String) body.get("description");

        getGroupChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            GroupChat groupChat = groupChatTable.createGroupChat(creator, name, description);
            response.put("statusCode", 200);
            response.put("msg", "Group chat created successfully");
            response.put("data", groupChat.toJson());

        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}

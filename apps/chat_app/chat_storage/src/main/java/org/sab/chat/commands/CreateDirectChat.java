package org.sab.chat.commands;

import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.tables.DirectChatTable;

import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class CreateDirectChat extends CommandWithVerification {

    private DirectChatTable directChatTable;

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
        Attribute firstMember = new Attribute("firstMember", DataType.STRING, true);
        Attribute secondMember = new Attribute("secondMember", DataType.STRING, true);

        return new Schema(List.of(firstMember, secondMember));
    }


    @Override
    public String execute() {

        UUID firstMember = UUID.fromString((String) body.get("firstMember"));
        UUID secondMember = UUID.fromString((String) body.get("secondMember"));

        getDirectChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            DirectChat directChat = directChatTable.createDirectChat(firstMember, secondMember);
            response.put("statusCode", 200);
            response.put("msg", "Direct chat created successfully");
            response.put("data", directChat.toJson());

        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();


    }

}


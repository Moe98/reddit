package org.sab.chat.commands;

import org.json.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.tables.DirectChatTable;

import org.sab.service.Command;

import java.util.UUID;

public class CreateDirectChat extends Command{

    private DirectChatTable directChat;

    public void getDirectChatTableInstance() {
        if (directChat == null) {
            CassandraConnector cassandra = new CassandraConnector();
            cassandra.connect();
            cassandra.initializeKeySpace();
            directChat = new DirectChatTable(cassandra);
            directChat.createTable();
        }
    }

    @Override
    public String execute(JSONObject request) {
        JSONObject requestBody = (JSONObject)request.get("body");
        UUID firstMember = UUID.fromString((String) requestBody.get("first_member"));
        UUID secondMember = UUID.fromString((String) requestBody.get("second_member"));

        getDirectChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            directChat.createDirectChat(firstMember, secondMember);
            response.put("statusCode", 200);
            response.put("msg", "Direct Chat Created");
        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        } finally {
            return response.toString();
        }

    }

}


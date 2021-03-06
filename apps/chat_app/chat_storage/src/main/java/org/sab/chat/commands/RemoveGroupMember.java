package org.sab.chat.commands;

import org.json.simple.JSONObject;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.tables.GroupChatTable;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.UUID;

public class RemoveGroupMember extends CommandWithVerification {

    private GroupChatTable groupChatTable;

    public void getGroupChatTableInstance() {
        if (groupChatTable == null) {
            CassandraConnector cassandra = CassandraConnector.getInstance();
            groupChatTable = cassandra.getGroupChatTable();
        }
    }

    @Override
    protected Schema getSchema() {

        Attribute chatId = new Attribute("chatId", DataType.UUID, true);
        Attribute adminId = new Attribute("adminId", DataType.UUID, true);
        Attribute memberId = new Attribute("memberId", DataType.UUID, true);

        return new Schema(List.of(chatId, adminId, memberId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    public String execute() {

        UUID chatId = UUID.fromString((String) body.get("chatId"));
        UUID adminId = UUID.fromString((String) body.get("adminId"));
        UUID memberId = UUID.fromString((String) body.get("memberId"));

        getGroupChatTableInstance();

        JSONObject response = new JSONObject();
        try {
            GroupChat groupChat = groupChatTable.removeGroupMember(chatId, adminId, memberId);

            JSONObject data = groupChat.toJson();
            data.put("targetMemberId", memberId.toString());

            response.put("statusCode", 200);
            response.put("msg", "Admin removed member successfully");
            response.put("data", data);
        } catch (InvalidInputException e) {
            response.put("statusCode", 400);
            response.put("msg", e.getMessage());
        }
        return response.toString();

    }

}

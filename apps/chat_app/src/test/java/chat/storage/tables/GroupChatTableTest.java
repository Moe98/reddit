package chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.apache.cassandra.service.ClientState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.tables.GroupChatTable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class GroupChatTableTest {

    private CassandraConnector cassandra;
    private GroupChatTable groupChats;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingChatTable_thenCreatedCorrectly() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + GroupChatTable.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(ColumnDefinitions.Definition::getName)
                        .collect(Collectors.toList());

        assertEquals(6, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("name"));
        assertTrue(columnNames.contains("description"));
        assertTrue(columnNames.contains("members"));
        assertTrue(columnNames.contains("admin"));
        assertTrue(columnNames.contains("date_created"));
    }

    @Test
    public void whenCreatingGroupChat_thenCreatedCorrectly() {
        String groupName = "Alpha Squad";
        String groupDesc = "Aqwa Squad fe Masr";
        UUID admin = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(admin, groupName, groupDesc);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chatId);

        assertEquals(groupName, createdGroupChat.getName());
        assertEquals(groupDesc, createdGroupChat.getDescription());
        assertEquals(admin, createdGroupChat.getAdmin());
        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenCreatingGroupChatWithInvalidData_thenFailsToCreate() {
        String[] groupNames = {"Alpha Squad", "", null};
        String[] groupDescs = {null, "Aqwa Squad fe Masr", "Cats"};
        for (int i = 0; i < 3; i++) {
            UUID admin = UUID.randomUUID();
            try {
                groupChats.createGroupChat(admin, groupNames[i], groupDescs[i]);
                fail("Created group chat with invalid data");
            } catch (InvalidInputException ignored) {

            }
        }
    }

    @Test
    public void whenAddingGroupMember_thenAddedCorrectly() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID user = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chat_id, admin, user);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chat_id);
        List<UUID> members = createdGroupChat.getMembers();
        if (!members.contains(user))
            fail("Member not added to group chat");

        groupChats.getMapper().delete(chat_id);

    }

    @Test
    public void whenAddingExsitingGroupMember_thenAddingFailed() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID user = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chat_id, admin, user);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.addGroupMember(chat_id, admin, user);
            fail("Failed to add an already existing member to the group chat");
        } catch (InvalidInputException e) {

        }


        groupChats.getMapper().delete(chat_id);

    }

    @Test
    public void whenRemovingGroupMember_thenRemovedCorrectly() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID user = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chat_id, admin, user);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.removeGroupMember(chat_id, admin, user);
        } catch (InvalidInputException e) {
            fail("Failed to remove a member to the group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chat_id);
        List<UUID> members = createdGroupChat.getMembers();
        if (members.contains(user))
            fail("Failed to remove member");

        groupChats.getMapper().delete(chat_id);

    }
    @Test
    public void whenRemovingNonExistingGroupMember_thenRemovingFailed() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.removeGroupMember(chat_id, admin, UUID.randomUUID());
            fail("Failed to remove a non existing member from the group chat");
        } catch (InvalidInputException e) {

        }

        groupChats.getMapper().delete(chat_id);

    }
    @Test
    public void whenAdminLeavesAGroup_thenLeavesSuccessfully() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chat_id, admin);
        } catch (InvalidInputException e) {
            fail("Failed to leave chat");
        }
        GroupChat createdGroupChat = groupChats.getMapper().get(chat_id);

        if(createdGroupChat!=null){
            fail("Admin failed to leave chat");
        }

    }

    @Test
    public void whenMemberLeavesAGroup_thenLeavesSuccessfuly() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID user = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chat_id, admin, user);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chat_id, user);
        } catch (InvalidInputException e) {
            fail("Failed to leave chat");
        }
        GroupChat createdGroupChat = groupChats.getMapper().get(chat_id);
        List<UUID> members = createdGroupChat.getMembers();
        if (members.contains(user))
            fail("Failed to leave group chat");

        groupChats.getMapper().delete(chat_id);
    }
    @Test
    public void whenNonExistingMemberLeavesAGroup_thenLeavingFailed() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = groupChats.createGroupChat(admin, name, description);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chat_id, UUID.randomUUID());
            fail("Non existing member failed to leave chat");
        } catch (InvalidInputException e) {

        }
        groupChats.getMapper().delete(chat_id);
    }
}

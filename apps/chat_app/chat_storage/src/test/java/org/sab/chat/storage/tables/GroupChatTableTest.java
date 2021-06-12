package org.sab.chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.models.GroupChat;

import java.util.ArrayList;
import java.util.Collections;
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
    public void checkGroupChatTableExists() {
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
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, groupName, groupDesc).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chatId);

        assertEquals(groupName, createdGroupChat.getName());
        assertEquals(groupDesc, createdGroupChat.getDescription());
        assertEquals(adminId, createdGroupChat.getAdmin());
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
                fail("Created group chat with invalid data.");
            } catch (InvalidInputException e) {
                assertEquals(e.getMessage(), i == 0 ? "Description cannot be null." : "Group name cannot be empty or null.");
            }
        }
    }

    @Test
    public void whenAddingGroupMember_thenAddedCorrectly() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID memberId = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chatId, adminId, memberId);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chatId);
        List<UUID> members = createdGroupChat.getMembers();
        if (!members.contains(memberId))
            fail("Member not added to group chat.");

        groupChats.getMapper().delete(chatId);

    }

    @Test
    public void whenAddingExistingGroupMember_thenAddingFailed() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID memberId = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chatId, adminId, memberId);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.addGroupMember(chatId, adminId, memberId);
            fail("Added an already existing member to the group chat.");
        } catch (InvalidInputException e) {
            assertEquals(e.getMessage(), "Member already there");
        }

        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenRemovingGroupMember_thenRemovedCorrectly() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID user = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chatId, adminId, user);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.removeGroupMember(chatId, adminId, user);
        } catch (InvalidInputException e) {
            fail("Failed to remove a member to the group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chatId);
        List<UUID> members = createdGroupChat.getMembers();
        if (members.contains(user))
            fail("Failed to remove member");

        groupChats.getMapper().delete(chatId);

    }

    @Test
    public void whenRemovingNonExistingGroupMember_thenRemovingFailed() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.removeGroupMember(chatId, adminId, UUID.randomUUID());
            fail("Failed to remove a non existing member from the group chat");
        } catch (InvalidInputException e) {
            assertEquals(e.getMessage(), "Member not in group");
        }

        groupChats.getMapper().delete(chatId);

    }

    @Test
    public void whenAdminLeavesAGroup_thenLeavesSuccessfully() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chatId, adminId);
        } catch (InvalidInputException e) {
            fail("Failed to leave chat");
        }
        try {
            groupChats.getGroupChat(chatId);

        } catch (InvalidInputException e) {
            assertEquals(e.getMessage(), "This chat does not exist");
        }

    }

    @Test
    public void whenMemberLeavesAGroup_thenLeavesSuccessfully() {
        String name = "name";
        String description = "description";
        UUID admin = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(admin, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID memberId = UUID.randomUUID();
        try {
            groupChats.addGroupMember(chatId, admin, memberId);
        } catch (InvalidInputException e) {
            fail("Failed to add a member to the group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chatId, memberId);
        } catch (InvalidInputException e) {
            fail("Failed to leave chat");
        }
        GroupChat createdGroupChat = null;
        try {
            createdGroupChat = groupChats.getGroupChat(chatId);
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
            fail(e.getMessage());
        }
        List<UUID> members = createdGroupChat.getMembers();

        assertEquals(members.contains(memberId), false);

        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenNonExistingMemberLeavesAGroup_thenLeavingFailed() {
        String name = "name";
        String description = "description";
        UUID adminId = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        try {
            groupChats.leavesChat(chatId, UUID.randomUUID());
            fail("Non existing member failed to leave chat");
        } catch (InvalidInputException e) {
            assertEquals(e.getMessage(), "Member not in group");
        }
        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenHavingXGroupChats_UserGetsXGroupChats() {
        int chatNumber = 10;

        UUID adminId = UUID.randomUUID();
        ArrayList<UUID> listOfChatIds = new ArrayList<>();
        for (int i = 0; i < chatNumber; i++) {

            String groupName = "Alpha Squad";
            String groupDesc = "Aqwa Squad fe Masr";
            UUID chatId = null;
            try {
                chatId = groupChats.createGroupChat(adminId, groupName, groupDesc).getChat_id();
                listOfChatIds.add(chatId);
            } catch (InvalidInputException e) {
                fail("Failed to create group chat: " + e.getMessage());
            }

        }

        List<GroupChat> groupChatsList = null;
        try {
            groupChatsList = groupChats.getGroupChats(adminId);
        } catch (InvalidInputException e) {
            fail("Failed to retrieve user direct chats: " + e.getMessage());
        }


        assertEquals(chatNumber, groupChatsList.size());

        ArrayList<UUID> listOfRetrievedChatIds = new ArrayList<>();
        for (int i = 0; i < listOfChatIds.size(); i++) {
            listOfRetrievedChatIds.add(groupChatsList.get(i).getChat_id());
        }

        Collections.sort(listOfRetrievedChatIds);
        Collections.sort(listOfChatIds);

        for (int i = 0; i < listOfChatIds.size(); i++) {
            assertEquals(listOfChatIds.get(i), listOfRetrievedChatIds.get(i));
            groupChats.getMapper().delete(listOfChatIds.get(i));
        }

    }


}

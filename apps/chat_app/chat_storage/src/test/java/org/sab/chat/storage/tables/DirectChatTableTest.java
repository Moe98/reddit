package org.sab.chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class DirectChatTableTest {

    private CassandraConnector cassandra;
    private DirectChatTable directChats;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        directChats = new DirectChatTable(cassandra);
        directChats.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void checkChatTableExists() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + DirectChatTable.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(ColumnDefinitions.Definition::getName)
                        .collect(Collectors.toList());

        assertEquals(3, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("first_member"));
        assertTrue(columnNames.contains("second_member"));
    }

    @Test
    public void whenCreatingDirectChat_thenCreatedCorrectly() {
        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();

        UUID chatId = null;
        try {
            chatId = directChats.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }

        DirectChat createdDirectChat = directChats.getMapper().get(chatId);

        assertEquals(chatId, createdDirectChat.getChat_id());
        assertEquals(firstMember, createdDirectChat.getFirst_member());
        assertEquals(secondMember, createdDirectChat.getSecond_member());
        directChats.getMapper().delete(chatId);

    }

    @Test
    public void whenCreatingDirectChatAlreadyExisting_thenFailsToCreate() {
        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();

        UUID chatId = null;
        try {
            chatId = directChats.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }

        try {
            directChats.createDirectChat(firstMember, secondMember);
            fail("Creating group chat with same members (duplicate)");
        } catch (InvalidInputException ignored) {
            assertEquals(ignored.getMessage(),"Chat already exist between Users" );
        }

        directChats.getMapper().delete(chatId);
    }

    @Test
    public void whenHavingXDirectChats_UserGetsXChats() {
        UUID firstMember = UUID.randomUUID();
        int chatNumber = 10;

        ArrayList<UUID> listOfChatIds = new ArrayList<>();
        for (int i = 0; i < chatNumber; i++) {

            UUID newMember = UUID.randomUUID();
            UUID chatId = null;
            try {
                chatId = directChats.createDirectChat(firstMember, newMember).getChat_id();
                listOfChatIds.add(chatId);
            } catch (InvalidInputException e) {
                fail("Failed to create direct chat: " + e.getMessage());
            }

        }

        List<DirectChat> directChatsList = null;
        try {
            directChatsList = directChats.getDirectChats(firstMember);
        } catch (InvalidInputException e) {
            fail("Failed to retrieve user direct chats: " + e.getMessage());
        }


        assertEquals(chatNumber, directChatsList.size());
        ArrayList<UUID> listOfRetrievedChatIds = new ArrayList<>();
        for(int i=0;i<listOfChatIds.size();i++){
            listOfRetrievedChatIds.add(directChatsList.get(i).getChat_id());
        }

        Collections.sort(listOfRetrievedChatIds);
        Collections.sort(listOfChatIds);

        for (int i=0;i<listOfChatIds.size();i++){
            assertEquals(listOfChatIds.get(i),listOfRetrievedChatIds.get(i));
            directChats.getMapper().delete(listOfChatIds.get(i));
        }

    }


}

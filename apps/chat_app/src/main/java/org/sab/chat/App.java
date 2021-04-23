package org.sab.chat;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;

import org.sab.chat.storage.models.Chat;
import org.sab.chat.storage.models.Message;
import org.sab.chat.storage.tables.ChatTable;
import org.sab.chat.storage.tables.MessageTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();
        System.out.println("Cassandra Connected");

        ChatTable chats = new ChatTable(cassandra);
        chats.createTable();

        MessageTable messages = new MessageTable(cassandra);
        messages.createTable();

        MappingManager manager = new MappingManager(cassandra.getSession());
        Mapper<Chat> mapper = manager.mapper(Chat.class);

        UUID chat_id = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        List<UUID> members = new ArrayList<>();
        members.add(admin);
        members.add(chat_id);
        mapper.save(new Chat(chat_id, "usta","yassta", members, admin));

        Chat chat = mapper.get(chat_id);
        System.out.println(chat);
        mapper.delete(chat_id);
        Mapper<Message> messageMapper = manager.mapper(Message.class);
        chat_id = UUID.randomUUID();
        UUID message_id = UUIDs.timeBased();
        UUID sender_id = UUID.randomUUID();
        messageMapper.save(new Message(chat_id, message_id, sender_id, "MAN"));

        Message message = messageMapper.get(chat_id,message_id);
        System.out.println(message);
        messageMapper.delete(chat_id,message_id);


        cassandra.close();
        System.out.println("Cassandra Closed");

    }
}

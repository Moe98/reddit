package org.sab.chat;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.models.Chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect();
        System.out.println("Cassandra Connected");


        MappingManager manager = new MappingManager(cassandra.getSession());
        Mapper<Chat> mapper = manager.mapper(Chat.class);

        //save person
        UUID chat_id = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        List<UUID> members = new ArrayList<>();
        members.add(admin);
        members.add(chat_id);
        mapper.save(new Chat(chat_id, "usta","yassta", members, admin));

        Chat chat = mapper.get(chat_id);
        System.out.println(chat);
        mapper.delete(chat_id);


        cassandra.close();
        System.out.println("Cassandra Closed");

    }
}

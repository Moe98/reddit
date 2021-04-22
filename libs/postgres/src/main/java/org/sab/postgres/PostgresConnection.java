package org.sab.postgres;

import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private Connection conn;
    private final URL configPath = getClass().getClassLoader().getResource("config.json");
    private PostgresConnection() {
    }

    public static PostgresConnection getInstance(){
        if(instance == null){
            instance = new PostgresConnection();
            instance.loadProperties();
        }
        return instance;
    }
    private void loadProperties(){
        JSONParser parser = new JSONParser();
        JSONObject propertiesJson = null;
        try {
            Object obj = parser.parse(new FileReader(configPath.getFile()));

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            JSONObject jsonObject = (JSONObject) obj;
            propertiesJson = jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        props = new Properties();
        props.setProperty("user",(String) propertiesJson.get("POSTGRES_USER"));
        props.setProperty("password",(String) propertiesJson.get("POSTGRES_PASSWORD"));
        url =
        String.format(
                "jdbc:postgresql://%s:%s/%s",
                (String) propertiesJson.get("POSTGRES_HOST"),
                (String) propertiesJson.get("POSTGRES_PORT"),
                (String) propertiesJson.get("POSTGRES_DB"));
    }

    public Connection connect() {
        try {
            conn = DriverManager.getConnection(url, props);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void closeConnection(Connection c) {
        try
        {
            c.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }    
    }
    public void run(String storedProcedure, List<Object> arguments){
        try {
            final PreparedStatement stmt = conn.prepareStatement("call " + storedProcedure + "(?)");
            for (int i = 0; i < arguments.size(); i++) {
                stmt.setObject(i + 1, arguments.get(i));
            }
            stmt.execute();
            stmt.close();
        } catch (Exception err) {
            System.out.println("An error has occurred.");
            System.out.println("See full details below.");
            err.printStackTrace();
        }
    }
}

package org.sab.postgres;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private Connection conn;
    private final URL configPath = getClass().getClassLoader().getResource("config.json");
    private final String[] propertiesParams = {"POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_HOST", "POSTGRES_PORT", "POSTGRES_DB"};

    private PostgresConnection() {
    }

    public static PostgresConnection getInstance() throws PropertiesNotLoadedException {
        if (instance == null) {
            instance = new PostgresConnection();
            try {
                instance.loadProperties();
            } catch (IOException | ParseException e) {
                throw new PropertiesNotLoadedException(e);
            }
        }
        return instance;
    }

    private void loadProperties() throws IOException, ParseException, PropertiesNotLoadedException {
        JSONParser parser = new JSONParser();
        JSONObject propertiesJson = (JSONObject) parser.parse(new FileReader(configPath.getFile()));
        props = new Properties();
        for (String param : propertiesParams)
            if (!propertiesJson.containsKey(param))
                throw new PropertiesNotLoadedException(String.format("%s is not found in the config.json", param));
        props.setProperty("user", (String) propertiesJson.get("POSTGRES_USER"));
        props.setProperty("password", (String) propertiesJson.get("POSTGRES_PASSWORD"));
        url =
                String.format(
                        "jdbc:postgresql://%s:%s/%s",
                        propertiesJson.get("POSTGRES_HOST"),
                        propertiesJson.get("POSTGRES_PORT"),
                        propertiesJson.get("POSTGRES_DB"));

    }

    public Connection connect() {
        try {
            conn = DriverManager.getConnection(url, props);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }

    public void closeConnection(Connection c) {
        try {
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void run(String storedProcedure, List<Object> arguments) {
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

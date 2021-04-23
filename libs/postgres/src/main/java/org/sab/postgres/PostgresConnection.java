package org.sab.postgres;

//import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.parser.ParseException;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private Connection conn;
    private final URL configPath = getClass().getClassLoader().getResource("config.json");
    private final String[] propertiesParams = {"POSTGRES_DB","POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_HOST", "POSTGRES_PORT"};
//    Dotenv dotenv = Dotenv.configure().load();

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
        //        JSONObject propertiesJson = (JSONObject) parser.parse(new FileReader(configPath.getFile()));
        props = new Properties();
//        if(System.getenv("POSTGRES_USER")==null)
//            throw new PropertiesNotLoadedException("I can read the secrets!!!!!!!!!!!");
//        for (String param : propertiesParams)
//            if (System.getenv(param)==null)
//                throw new PropertiesNotLoadedException(String.format("%s is not an environment variable", param));
        System.out.println(System.getenv("POSTGRES_DB"));
        props.setProperty("user", System.getenv("POSTGRES_USER"));
        props.setProperty("password", System.getenv("POSTGRES_PASSWORD"));
        url =
                String.format(
                        "jdbc:postgresql://%s:%s/%s",
                        System.getenv("POSTGRES_HOST"),
                        System.getenv("POSTGRES_PORT"),
                        System.getenv("POSTGRES_DB"));
    }

    public Connection connect() {
        System.out.println(url);
        try{
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

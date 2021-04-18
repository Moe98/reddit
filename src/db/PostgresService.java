package db;

import java.io.FileReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.PreparedStatement;

public class PostgresService {

    private static final String PROPERTIES_FILE = "./postgres_properties.json";
    private static PostgresService instance = null;

    private String url;
    private Properties props;
    private Connection conn;

    private PostgresService() {
    }

    public static PostgresService getInstance(){
        if(instance == null){
            instance = new PostgresService();
            instance.init();
        }
        return instance;
    }

    private void init() {
        loadProperties();
        connect();
    }

    private void loadProperties(){
        final JSONParser parser = new JSONParser();
        JSONObject propertiesJson = null;
        try {
            propertiesJson = (JSONObject) parser.parse(new FileReader(PROPERTIES_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        url = propertiesJson.getString("url");

        props = new Properties();
        props.setProperty("user", propertiesJson.getString("user"));
        props.setProperty("password", propertiesJson.getString("password"));
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(url, props);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
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

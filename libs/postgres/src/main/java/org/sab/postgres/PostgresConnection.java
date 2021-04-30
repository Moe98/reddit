package org.sab.postgres;

//import io.github.cdimascio.dotenv.Dotenv;

import org.json.simple.parser.ParseException;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private Connection conn;
    private final URL configPath = getClass().getClassLoader().getResource("config.json");
    private final String[] propertiesParams = {"POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_HOST", "POSTGRES_PORT"};
//    Dotenv dotenv = Dotenv.configure().load();

    private PostgresConnection() {
    }

    public static PostgresConnection getInstance() throws PropertiesNotLoadedException {
        if (instance == null) {
            final PostgresConnection attemptedConnection = new PostgresConnection();
            try {
                attemptedConnection.loadProperties();
                instance = attemptedConnection;
            } catch (IOException | ParseException e) {
                throw new PropertiesNotLoadedException(e);
            }
        }
        return instance;
    }

    private void loadProperties() throws IOException, ParseException, PropertiesNotLoadedException {
        //        JSONObject propertiesJson = (JSONObject) parser.parse(new FileReader(configPath.getFile()));
        props = new Properties();

        for (String param : propertiesParams)
            if (System.getenv(param) == null)
                throw new PropertiesNotLoadedException(String.format("%s is not an environment variable", param));
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

    public static String procedureInitializer(String procedureName, int numParams) {
        StringBuilder ans = new StringBuilder("{").append("call");
        ans.append(" ").append(procedureName).append("(");
        for (int i = 0; i < numParams; i++)
            ans.append("?").append(i == numParams - 1 ? ")" : ",");

        ans.append("}");
        return ans.toString();

    }

    public static ResultSet call(String procedureName, Object... params) throws PropertiesNotLoadedException, SQLException {

        PostgresConnection postgresConnection = getInstance();
        Connection connection = postgresConnection.connect();
        ResultSet resultSet = postgresConnection.call(procedureInitializer(procedureName, params.length), connection, null, params);
        postgresConnection.closeConnection(connection);
        return  resultSet;
    }

    public ResultSet call(String sql, Connection connection, int[] types, Object... params) throws SQLException {


        // DbUtils does not support calling procedures?
        CallableStatement callableStatement = connection.prepareCall(sql);

        for (int i = 0; i < params.length; i++) {
            callableStatement.setObject(i + 1, params[i]);
        }
        boolean containsResults = callableStatement.execute();
        ResultSet resultSet = null;
        if (containsResults) {
            resultSet = callableStatement.getResultSet();
        }
        return resultSet;
    }
}

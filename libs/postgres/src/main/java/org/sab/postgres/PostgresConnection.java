package org.sab.postgres;


import org.json.simple.parser.ParseException;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.io.*;
import java.sql.*;

import java.util.Properties;


public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private Connection conn;
    private final String[] propertiesParams = {"POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_HOST", "POSTGRES_PORT"};

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
        return resultSet;
    }

    public ResultSet call(String sql, Connection connection, int[] types, Object... params) throws SQLException {


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

    private static void createUsersTable() throws IOException {
        runScript("../../libs/postgres/src/main/resources/sql/CreateTable.sql");
    }

    private static void createUsersProcedures() throws IOException {
        runScript("../../libs/postgres/src/main/resources/sql/procedures.sql");
    }

    private static void runScript(String scriptPath) throws IOException {
        String[] command = new String[]{
                "psql",
                "-f",
                scriptPath,
                "postgresql://postgres:12345678@localhost:5432/postgres"
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null)
            System.out.println(line);


    }

    public static void dbInit() throws IOException {
        createUsersTable();
        createUsersProcedures();

    }


}
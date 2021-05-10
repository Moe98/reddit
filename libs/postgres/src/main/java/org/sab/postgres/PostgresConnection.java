package org.sab.postgres;


import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

public class PostgresConnection {
    private static PostgresConnection instance = null;

    private String url;
    private Properties props;
    private static final String[] propertiesParams = {"POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_HOST", "POSTGRES_PORT"};

    private PostgresConnection() {
    }


    public static PostgresConnection getInstance() throws EnvironmentVariableNotLoaded {
        if (instance == null) {
            final PostgresConnection attemptedConnection = new PostgresConnection();
            attemptedConnection.loadProperties();
            instance = attemptedConnection;

        }
        return instance;
    }

    private void loadProperties() throws EnvironmentVariableNotLoaded {
        props = new Properties();

        for (String param : propertiesParams)
            if (System.getenv(param) == null)
                throw new EnvironmentVariableNotLoaded(param);
        props.setProperty("user", System.getenv("POSTGRES_USER"));
        props.setProperty("password", System.getenv("POSTGRES_PASSWORD"));
        url =
                String.format(
                        "jdbc:postgresql://%s:%s/%s",
                        System.getenv("POSTGRES_HOST"),
                        System.getenv("POSTGRES_PORT"),
                        System.getenv("POSTGRES_DB"));
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, props);
    }


    public static String procedureInitializer(String procedureName, int numParams) {
        StringBuilder ans = new StringBuilder("{").append("call");
        ans.append(" ").append(procedureName).append("(");
        for (int i = 0; i < numParams; i++)
            ans.append("?").append(i == numParams - 1 ? ")" : ",");

        ans.append("}");
        return ans.toString();

    }

    public static ResultSet call(String procedureName, Object... params) throws SQLException, EnvironmentVariableNotLoaded {

        PostgresConnection postgresConnection = getInstance();
        Connection connection = postgresConnection.connect();
        try {
            ResultSet resultSet = postgresConnection.call(procedureInitializer(procedureName, params.length), connection, params);
            connection.close();
            return resultSet;
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
    }

    public ResultSet call(String sql, Connection connection, Object... params) throws SQLException {


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

    private static void createUsersTable(boolean isTesting) throws IOException, EnvironmentVariableNotLoaded {
        String path = "libs/postgres/src/main/resources/sql/CreateUsersTable.sql";
        if (isTesting)
            path = "../../" + path;
        runScript(path);
    }

    private static void createUsersProcedures(boolean isTesting) throws IOException, EnvironmentVariableNotLoaded {
        String path = "libs/postgres/src/main/resources/sql/CreateUserProecuderes.sql";
        if (isTesting)
            path = "../../" + path;
        runScript(path);
    }

    private static void runScript(String scriptPath) throws IOException, EnvironmentVariableNotLoaded {
        for (String param : propertiesParams)
            if (System.getenv(param) == null)
                throw new EnvironmentVariableNotLoaded(param);
        String[] command = new String[]{
                "psql",
                "-f",
                scriptPath,
                String.format("postgresql://%s:%s@%s:%s/%s", System.getenv("POSTGRES_USER"),
                        System.getenv("POSTGRES_PASSWORD"), System.getenv("POSTGRES_HOST"),
                        System.getenv("POSTGRES_PORT"), System.getenv("POSTGRES_DB"))
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null)
            System.out.println(line);


    }

    public static void dbInit(boolean isTesting) throws IOException, EnvironmentVariableNotLoaded {
        createUsersTable(isTesting);
        createUsersProcedures(isTesting);

    }


}

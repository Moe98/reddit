package org.sab.postgres;


import org.sab.auth.Auth;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

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

    Connection connect() throws SQLException {
        return DriverManager.getConnection(url, props);
    }


    private static String procedureInitializer(String procedureName, int numParams) {
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
        ResultSet resultSet;
        try {
            resultSet = postgresConnection.call(procedureInitializer(procedureName, params.length), connection, params);
            connection.close();
        } finally {
            connection.close();
        }
        return resultSet;
    }

    private ResultSet call(String sql, Connection connection, Object... params) throws SQLException {


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

    private static void createUsersTable() throws IOException, EnvironmentVariableNotLoaded {

        runScript(getScriptPath("CreateUsersTable"));
    }

    private static void createUsersProcedures() throws IOException, EnvironmentVariableNotLoaded {
        runScript(getScriptPath("CreateUserProcedures"));
    }

    private static void createMockData() throws IOException, EnvironmentVariableNotLoaded {
        for (int rowNumber = 0; rowNumber < 10000; rowNumber++) {
            String username = "user" + rowNumber;
            String userId = UUID.randomUUID().toString();
            String hashedPassword = Auth.hash("12345678");
            String email = "user" + rowNumber + "@gmail.com";
            Date birthdate = Date.valueOf("1998-1-1");

            try {
                PostgresConnection.call("create_user", userId, username, email, hashedPassword, birthdate);
                System.out.println("user" + rowNumber + " Inserted");
            } catch (EnvironmentVariableNotLoaded | SQLException e) {
                System.out.println("Error Occured while adding user number " + rowNumber);
            }
        }
    }
    private static void runMockDataProcedure() throws EnvironmentVariableNotLoaded, SQLException {
        PostgresConnection postgresConnection = getInstance();
        Connection connection = postgresConnection.connect();
            try {
                connection.prepareCall("{call mockData()}").execute();
            } catch ( SQLException e) {
                System.out.println(e);
                System.out.println("Error Occured while adding users mock data ");
            }finally {
                connection.close();
            }
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

    public static void dbInit() throws IOException, EnvironmentVariableNotLoaded, SQLException {
        createUsersTable();
        createUsersProcedures();
        runMockDataProcedure();
    }

    private static String getScriptPath(String sqlScriptName) {
        ClassLoader classLoader = PostgresConnection.class.getClassLoader();
        File file = new File(classLoader.getResource("sql/" + sqlScriptName + ".sql").getFile());
        return file.getAbsolutePath();
    }

}

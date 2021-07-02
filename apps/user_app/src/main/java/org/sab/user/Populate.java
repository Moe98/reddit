package org.sab.user;

import org.sab.postgres.PostgresConnection;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;

public class Populate {

    static final int NUM_USERS = 10 * 1000;

    public static void main(String[] args) {
        try {
            PostgresConnection.call("mockData", NUM_USERS);
        } catch (SQLException | EnvironmentVariableNotLoaded e) {
            e.printStackTrace();
        }
        
    }
}

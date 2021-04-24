package org.sab.postgres;

import org.junit.Test;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

public class PostgresConnectionTest {
    public void runDB() {
        try {
            PostgresConnection pg = PostgresConnection.getInstance();
            Connection c = pg.connect();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT 1");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            assertFalse(rs.next());
        } catch (PropertiesNotLoadedException | SQLException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void postgresWorking() {
        runDB();
    }

    @Test
    public void postgresIsSingleton()  {
        PostgresConnection conn1 = null;
        PostgresConnection conn2 = null;
        try {
            conn1 = PostgresConnection.getInstance();
            conn2 = PostgresConnection.getInstance();
        } catch (PropertiesNotLoadedException e) {
            fail(e.getMessage());
        }
        assertTrue(conn1 == conn2);
    }

    @Test
    public void canCloseConnection() {
        PostgresConnection postgresConnection = null;
        try {
            postgresConnection = PostgresConnection.getInstance();
        } catch (PropertiesNotLoadedException e) {
            fail(e.getMessage());
        }
        Connection conn = postgresConnection.connect();
        postgresConnection.closeConnection(conn);
    }



}

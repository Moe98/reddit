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
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void postgresWorking() {
        runDB();
    }

    @Test
    public void postgresIsSingleton() throws PropertiesNotLoadedException {
        PostgresConnection conn1 = PostgresConnection.getInstance();
        PostgresConnection conn2 = PostgresConnection.getInstance();
        assertTrue(conn1 == conn2);
    }

    @Test
    public void canCloseConnection() throws PropertiesNotLoadedException {
        PostgresConnection postgresConnection = PostgresConnection.getInstance();
        Connection conn = postgresConnection.connect();
        postgresConnection.closeConnection(conn);
    }



}

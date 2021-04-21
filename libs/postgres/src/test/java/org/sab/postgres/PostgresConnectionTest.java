package org.sab.postgres;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.sab.postgres.PostgresConnection;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static org.junit.Assert.*;

public class PostgresConnectionTest{
    public void runDB() {
        try {
            PostgresConnection pg = PostgresConnection.getInstance();
            Connection c = pg.connect();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT 1");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            assertFalse(rs.next());
        } catch (Exception e) {
            System.out.print(e);
            fail();
        }
    }

    @Test
    public void postgresWorking() {
        runDB();
    }

}

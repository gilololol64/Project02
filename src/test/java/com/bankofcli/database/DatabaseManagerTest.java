package com.bankofcli.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Mockito.times;

public class DatabaseManagerTest {

    public DatabaseManager dbm;
    private static final String url="jdbc:sqlite:BigBankersBank.db";

    @BeforeEach
    public void setup(){
        dbm = new DatabaseManager();
    }

    @Test
    public void openPositive() throws RuntimeException, SQLException{

        Connection conn = Mockito.mock(Connection.class);

        try(MockedStatic<DriverManager> dm = Mockito.mockStatic(DriverManager.class)){
            dm.when(() -> DriverManager.getConnection(url)).thenReturn(conn);

            try(Connection con = dbm.open()) {
                Assertions.assertNotNull(con);
            }
        }
    }

    @Test
    public void openRuntimeException(){
        String expectedMessage = "Could not connect to database.";
        SQLException expectedEx = new SQLException("Connection Failed");

        //Stub DriverManager
        try(MockedStatic<DriverManager> dm = Mockito.mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(url)).thenThrow(expectedEx);

            RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                    () -> dbm.open());
            Assertions.assertEquals(expectedMessage, ex.getMessage());
        }
    }

    @Test
    public void closePositive() throws SQLException {
        Connection conn = Mockito.mock(Connection.class);
        dbm.close(conn);
        Mockito.verify(conn, times(1)).close();
    }

    @Test
    public void closeNullPointerException(){
        String expectedMessage = "Could not close empty database connection.";
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> dbm.close(null));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void closeRuntimeException() throws SQLException {
        String expectedMessage = "Could not close connection.";
        Connection conn = Mockito.mock(Connection.class);
        Mockito.doThrow(new SQLException()).when(conn).close();

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> dbm.close(conn));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }


}

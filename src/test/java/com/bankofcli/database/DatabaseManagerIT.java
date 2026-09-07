package com.bankofcli.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManagerIT {

    public DatabaseManager dbm;
    private static final String url="jdbc:sqlite:BigBankersBank.db";

    @BeforeEach
    public void setup(){
        dbm = new DatabaseManager();
    }

    @Test
    public void openPositive() throws RuntimeException, SQLException {
        try(Connection con = dbm.open()) {
            Assertions.assertNotNull(con);
        }
    }

    @Test
    public void closePositive() throws SQLException {
        try(Connection con = dbm.open()){
            dbm.close(con);
        }
    }

    @Test
    public void closeNullPointerException() throws SQLException{
        Connection con = null;
        String expectedMessage = "Could not close empty database connection.";
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> dbm.close(con));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    public void positiveInit() throws SQLException {
        String names [] = {"accounts", "transactions"};
        dbm.init();

        try(Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement()) {
            String verifyTableNamesQry = "SELECT name FROM sqlite_schema WHERE type='table';";
            Set<String> actualTableNames = new HashSet<>();

            try(ResultSet rs = stmt.executeQuery(verifyTableNamesQry)){
                while(rs.next()) {
                    actualTableNames.add(rs.getString("name"));
                }
            }

            for(String name : names){
                Assertions.assertTrue(actualTableNames.contains(name),
                        String.format("The '%s' table was not present in the database.", name));
            }
        }
    }
}

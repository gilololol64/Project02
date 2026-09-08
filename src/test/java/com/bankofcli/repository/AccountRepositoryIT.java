package com.bankofcli.repository;

import com.bankofcli.model.Account;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class AccountRepositoryIT {

    public AccountRepository accRepo;
    public List<Account> accountList;
    public List<Long> testAccountIDs;
    private static final String url="jdbc:sqlite:BigBankersBank.db";

    @BeforeEach
    public void setup(){
        //Set up testing variables
        int pin = 1111;
        int balance = 0;
        accRepo = new AccountRepository();
        testAccountIDs = new ArrayList<>();
        accountList = new ArrayList<>();

        //Use stream to make list of longs 0 - (-5) and for each add to the test account list
        LongStream.iterate(0, i -> i >= -5, i -> i - 1)
                .forEachOrdered(testAccountIDs::add);
        //Create dummy accounts for testing
        for (Long accID : testAccountIDs){
            accountList.add(new Account(accID, pin, balance));
        }
    }

    @AfterEach
    public void teardown() throws SQLException {
        // Delete Test accounts (0, -1, -2, -3, -4, -5) from Database
        String removeTestAccountsQry = "DELETE FROM accounts WHERE account_id <= 0;";

        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement(removeTestAccountsQry)) {
            stmt.executeUpdate();
        }
    }

    @Test
    public void saveSinglePositive() throws SQLException {
        //Set up getting account object and preparing variables
        Account acc = accountList.getFirst();
        long accID = acc.getAccountID();
        Account result = null;
        String verifyAccountInsertedQry = "SELECT * FROM accounts WHERE account_id = ?";

        //Action Testing save method
        accRepo.save(acc);

        //Verifying results by running independent query against database
        try(Connection con = DriverManager.getConnection(url);
            PreparedStatement stmt = con.prepareStatement(verifyAccountInsertedQry)) {
            stmt.setLong(1, accID);

            //Collect results into account
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    long resultAccID = rs.getLong("account_id");
                    int resultPin = rs.getInt("pin");
                    int resultBalance = rs.getInt("balance");
                    result = new Account(resultAccID,resultPin, resultBalance);
                }
            }
        }
        //Asserting account is not null and that the accounts inserted has matching fields
        assertAccountsEquals(acc, result);
    }

    @Test
    public void saveListPositive() throws SQLException {
        //Set up list of result accounts and verifying query
        List<Account> results = new ArrayList<>();
        String verifyAccountsInsertedQry = "SELECT * FROM accounts WHERE account_id < 1";

        //Action Testing save method
        accRepo.save(accountList);

        //Verifying results by running independent query against database
        try(Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement()) {

            //Execute test query and store accounts in result List
            try(ResultSet rs = stmt.executeQuery(verifyAccountsInsertedQry)){
                while(rs.next()){
                    long resultAccID = rs.getLong("account_id");
                    int resultPin = rs.getInt("pin");
                    int resultBalance = rs.getInt("balance");
                    results.add(new Account(resultAccID,resultPin, resultBalance));
                }
            }

            //Check if any Accounts were inserted and if matching amount was
            Assertions.assertFalse(results.isEmpty(),
                    "No Accounts were inserted into database");
            Assertions.assertEquals(results.size(), accountList.size(),
                    "Improper Number of Accounts Inserted.");

            //Reverses order so that Accounts correspond with each other
            results = results.reversed();

            //Asserting account is not null and that the accounts inserted has matching fields
            for (int i = 0; i < results.size(); i++) {
                assertAccountsEquals(accountList.get(i), results.get(i));
            }
        }
    }


    @Test
    public void saveNullAccountList(){
        List<Account> acc = null;
        String expectedMessage = "Can not insert empty list of accounts";
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> accRepo.save(acc));
        Assertions.assertEquals(expectedMessage,ex.getMessage());
    }

    @Test
    public void saveNullAccount(){
        Account acc = null;
        String expectedMessage = "Can not insert an empty account";
        NullPointerException ex =
                Assertions.assertThrows(NullPointerException.class, () -> accRepo.save(acc));
        Assertions.assertEquals(expectedMessage,ex.getMessage());
    }

    @Test
    public void findByIDPositive() throws SQLException {
        Account acc = accountList.getFirst();
        long accId = acc.getAccountID();
        String insertTestAccountQry = "INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?)";
        Account result = null;

        try(Connection con = DriverManager.getConnection(url);
            PreparedStatement stmt = con.prepareStatement(insertTestAccountQry)) {
            stmt.setLong(1, accId);
            stmt.setInt(2, acc.getPin());
            stmt.setInt(3, acc.getBalanceExtendedCents());
            stmt.executeUpdate();
        }

        result = accRepo.findByID(accId);

        assertAccountsEquals(acc, result);
    }

    @Test
    public void findByIDNoAccountExists(){
        Account acc = accountList.getFirst();
        long accId = acc.getAccountID();

        Account result = accRepo.findByID(accId);
        Assertions.assertNull(result);
    }

    private void assertAccountsEquals(Account expected, Account result){
        //Asserting account is not null and that the accounts inserted has matching fields
        Assertions.assertNotNull(result, "Account not found in database after insertion");
        Assertions.assertEquals(expected, result, "Account IDs do not match.");
        Assertions.assertEquals(expected.getPin(), result.getPin(), "Account pins do not match");
        Assertions.assertEquals(expected.getBalanceExtendedCents(), result.getBalanceExtendedCents(),
                "Account balances do not match");
    }

}

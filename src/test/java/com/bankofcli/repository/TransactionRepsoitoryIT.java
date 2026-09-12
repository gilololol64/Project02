package com.bankofcli.repository;

import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class TransactionRepsoitoryIT {

    private static final String url="jdbc:sqlite:BigBankersBank.db";
    public List<Transaction> expectedTransList;
    public TransactionRepository transRepo;
    public AccountRepository accRepo;

    @BeforeEach
    public void setup(){

        //Set up testing variables
        expectedTransList = new ArrayList<>();
        transRepo = new TransactionRepository();
        accRepo = new AccountRepository();
    }

    @AfterEach
    public void teardown() throws SQLException {
        // Delete Test transactions from database with foreign keys (0, -1) from Database
        String removeTestAccounts = "DELETE FROM accounts WHERE account_id <= 0";
        String removeTestTransactionsSrcQry = "DELETE FROM transactions WHERE account_src <= 0";
        String removeTestTransactionsDstQry = "DELETE FROM transactions WHERE account_dst <= 0";

        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmtSrc = con.prepareStatement(removeTestTransactionsSrcQry);
             PreparedStatement stmtDst = con.prepareStatement(removeTestTransactionsDstQry);
             PreparedStatement stmtAcc = con.prepareStatement(removeTestAccounts)) {
            stmtSrc.executeUpdate();
            stmtDst.executeUpdate();
            stmtAcc.executeUpdate();
        }
    }

    @Test
    public void savedPositiveTransfer() throws SQLException {

        Account accountSrcO = new Account(-1L, 1111, 1000);
        accRepo.save(accountSrcO);
        Account accountDstO = new Account(0L, 1111, 0);
        accRepo.save(accountDstO);

        Transaction expectedTrans = new Transaction(1,
                Transaction.Type.TRANSFER,
                LocalDateTime.now(),
                1000,
                -1L,
                0L);

        transRepo.save(expectedTrans);

        String sql = "SELECT * FROM transactions WHERE account_src = -1 AND account_dst = 0";
        Transaction actualTrans = null;

        try(Connection con = DriverManager.getConnection(url);
            var stmt = con.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    long transID = rs.getLong("transaction_id");
                    String transType = rs.getString("trans_type");
                    Transaction.Type type = Transaction.Type.getTypeFromString(transType);
                    String rawDate = rs.getString("time_complete");
                    LocalDateTime timeComplete = LocalDateTime.parse(rawDate);
                    long amount = rs.getLong("amount");
                    Long accountSrc = (type != Transaction.Type.DEPOSIT) ? rs.getLong("account_src") : null;
                    Long accountDst = (type != Transaction.Type.WITHDRAW) ? rs.getLong("account_dst") : null;
                    actualTrans = new Transaction(transID, type, timeComplete, amount, accountSrc, accountDst);
                }
            }
        }

        Assertions.assertEquals(expectedTrans, actualTrans);
    }

    @Test
    public void savedPositiveWithdraw() throws SQLException {

        Account accountSrcO = new Account(-1L, 1111, 1000);
        accRepo.save(accountSrcO);

        Transaction expectedTrans = new Transaction(1,
                Transaction.Type.WITHDRAW,
                LocalDateTime.now(),
                1000,
                -1L,
                null);

        transRepo.save(expectedTrans);

        String sql = "SELECT * FROM transactions WHERE account_src = -1";
        Transaction actualTrans = null;

        try(Connection con = DriverManager.getConnection(url);
            var stmt = con.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    long transID = rs.getLong("transaction_id");
                    String transType = rs.getString("trans_type");
                    Transaction.Type type = Transaction.Type.getTypeFromString(transType);
                    String rawDate = rs.getString("time_complete");
                    LocalDateTime timeComplete = LocalDateTime.parse(rawDate);
                    long amount = rs.getLong("amount");
                    Long accountSrc = (type != Transaction.Type.DEPOSIT) ? rs.getLong("account_src") : null;
                    Long accountDst = (type != Transaction.Type.WITHDRAW) ? rs.getLong("account_dst") : null;
                    actualTrans = new Transaction(transID, type, timeComplete, amount, accountSrc, accountDst);
                }
            }
        }

        Assertions.assertEquals(expectedTrans, actualTrans);
    }

    @Test
    public void savedPositiveDeposit() throws SQLException {

        Account accountDstO = new Account(0L, 1111, 0);
        accRepo.save(accountDstO);

        Transaction expectedTrans = new Transaction(1,
                Transaction.Type.DEPOSIT,
                LocalDateTime.now(),
                1000,
                null,
                0L);

        transRepo.save(expectedTrans);

        String sql = "SELECT * FROM transactions WHERE account_dst = 0";
        Transaction actualTrans = null;

        try(Connection con = DriverManager.getConnection(url);
            var stmt = con.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    long transID = rs.getLong("transaction_id");
                    String transType = rs.getString("trans_type");
                    Transaction.Type type = Transaction.Type.getTypeFromString(transType);
                    String rawDate = rs.getString("time_complete");
                    LocalDateTime timeComplete = LocalDateTime.parse(rawDate);
                    long amount = rs.getLong("amount");
                    Long accountSrc = (type != Transaction.Type.DEPOSIT) ? rs.getLong("account_src") : null;
                    Long accountDst = (type != Transaction.Type.WITHDRAW) ? rs.getLong("account_dst") : null;
                    actualTrans = new Transaction(transID, type, timeComplete, amount, accountSrc, accountDst);
                }
            }
        }

        Assertions.assertEquals(expectedTrans, actualTrans);
    }

    @Test
    public void getAuditPositive() throws SQLException {
        long accountID = -1L;
        Account accountSrcO = new Account(accountID, 1111, 1000);
        accRepo.save(accountSrcO);
        Account accountDstO = new Account(0L, 1111, 0);
        accRepo.save(accountDstO);

        //Add transactions, give time for them to wait so dates are in order by most recent
        expectedTransList.add(new Transaction(1,
                Transaction.Type.TRANSFER,
                LocalDateTime.now(),
                1000,
                accountID,
                0L));
        sleep(1);

        expectedTransList.add(new Transaction(1,
                Transaction.Type.WITHDRAW,
                LocalDateTime.now(),
                500,
                accountID,
                null));
        sleep(1);

        expectedTransList.add(new Transaction(1,
                Transaction.Type.DEPOSIT,
                LocalDateTime.now(),
                5000,
                null,
                accountID));

        for (Transaction trans: expectedTransList){
            transRepo.save(trans);
        }
        expectedTransList = expectedTransList.reversed();

        List<Transaction> actualTransList = transRepo.getAudit(accountID);

        Assertions.assertEquals(expectedTransList.size(), actualTransList.size());

        for(int i = 0; i < expectedTransList.size(); i++){
            Assertions.assertEquals(expectedTransList.get(i), actualTransList.get(i));
        }
    }

    @Test
    public void getAuditNoTransactionHistory() throws SQLException {
        long accountID = -1L;
        List<Transaction> actualTransList = transRepo.getAudit(accountID);
        Assertions.assertNull(actualTransList);
    }

    private void sleep(int sec){
        try {
            // Wait for exactly 5 seconds
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}


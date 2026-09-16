package com.bankofcli.service;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransactionServiceIT {

    public TransactionService transServ;
    public AccountRepository accRepo;
    public TransactionRepository transRepo;
    public AccountService accServ;

    @BeforeEach
    public void setup(){
        accRepo = new AccountRepository();
        transRepo = new TransactionRepository();
        transServ = new TransactionService(accRepo, transRepo);
        accServ = new AccountService(accRepo);
    }

    @AfterEach
    public void teardown(){
        DatabaseManager db = new DatabaseManager();
        String deleteAccQry = "DELETE FROM accounts";
        String deleteTransQry = "DELETE FROM transactions";
        try(Connection con = db.open();
            PreparedStatement psAcc = con.prepareStatement(deleteAccQry);
            PreparedStatement psTrans = con.prepareStatement(deleteTransQry)) {
            psTrans.executeUpdate();
            psAcc.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void depositPositive(){
        long accID = 1111L;
        int pin = 1111;
        long balance = 0;
        long deposit = 2500;
        long expectedBalance = balance + deposit;

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        transServ.deposit(accID, deposit);
        Assertions.assertEquals(expectedBalance, accServ.getBalance(accID));
    }

    @Test
    public void depositNegativeAmount(){
        long accID = 1111L;
        long balance = 0;
        long deposit = -100;
        int pin = 1111;
        String expectedMessage = "Transaction amount must be greater than zero.";

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void depositMaxAmount(){
        long accID = 1111L;
        long balance = 1;
        long deposit = Long.MAX_VALUE;
        int pin = 1111;
        String expectedMessage = "Transaction amount too large to process";

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void depositAccountNotFoundException(){
        long accID = 1111L;
        long deposit = 100;
        String expectedMessage = "Account " + accID + " was not found.";

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawPositive(){
        long accID = 1111L;
        long balance = 2500;
        long withdraw = 2500;
        long expectedBalance = balance - withdraw;
        int pin = 1111;

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        transServ.withdraw(accID, withdraw);
        Assertions.assertEquals(expectedBalance, accServ.getBalance(accID));
    }

    @Test
    public void withdrawInsufficientFundsException(){
        long accID = 1111L;
        long balance = 2500;
        long withdraw = 2501;
        String expectedMessage = "Insufficient funds.";
        int pin = 1111;

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        InsufficientFundsException ex = Assertions.assertThrows(InsufficientFundsException.class,
                () ->transServ.withdraw(accID, withdraw));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawNegativeAmount(){
        long accID = 1111L;
        long balance = 0;
        long withdraw = -100;
        String expectedMessage = "Transaction amount must be greater than zero.";
        int pin = 1111;

        Account expectedAccount = new Account(accID, pin, balance);
        accRepo.save(expectedAccount);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.withdraw(accID, withdraw));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawAccountNotFoundException(){
        long accID = 1111L;
        long withdraw = 100;
        String expectedMessage = "Account " + accID + " was not found.";

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () ->transServ.withdraw(accID, withdraw));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void transferPositive(){
        long srcAccID = 1111L;
        long srcBalance = 10;
        long dstAccID = 1112L;
        long dstBalance = 0;
        long amount = 5;
        int pin = 1111;

        long expectedSrcBalance = srcBalance - amount;
        long expectedDstBalance = dstBalance + amount;

        Account expectedSrcAccount = new Account(srcAccID, pin, srcBalance);
        Account expectedDstAccount = new Account(dstAccID, pin, dstBalance);
        accRepo.save(expectedSrcAccount);
        accRepo.save(expectedDstAccount);

        transServ.transfer(srcAccID, dstAccID, amount);

        Assertions.assertEquals(expectedSrcBalance, accServ.getBalance(srcAccID));
        Assertions.assertEquals(expectedDstBalance, accServ.getBalance(dstAccID));
    }


    @Test
    public void transferInsufficientFundsException(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        long dstAccID = 1112L;
        long dstBalance = 0;
        long amount = 10;
        String expectedMessage = "Insufficient funds.";
        int pin = 1111;

        Account expectedSrcAccount = new Account(srcAccID, pin, srcBalance);
        Account expectedDstAccount = new Account(dstAccID, pin, dstBalance);
        accRepo.save(expectedSrcAccount);
        accRepo.save(expectedDstAccount);

        InsufficientFundsException ex = Assertions.assertThrows(InsufficientFundsException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void transferAccountNotFoundSrc(){
        long srcAccID = 1111L;
        long dstAccID = 1112L;
        long dstBalance = 0;
        long amount = 10;
        String expectedMessage = "Account " + srcAccID + " was not found.";
        int pin = 1111;

        Account expectedDstAccount = new Account(dstAccID, pin, dstBalance);
        accRepo.save(expectedDstAccount);

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void transferAccountNotFoundDst(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        long dstAccID = 1112L;
        long amount = 10;
        String expectedMessage = "Account " + dstAccID + " was not found.";
        int pin = 1111;

        Account expectedSrcAccount = new Account(srcAccID, pin, srcBalance);
        accRepo.save(expectedSrcAccount);

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void transferInvalidAmountException(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        long dstAccID = 1112L;
        long dstBalance = 0;
        long amount = -10;
        String expectedMessage = "Transaction amount must be greater than zero.";
        int pin = 1111;

        Account expectedSrcAccount = new Account(srcAccID, pin, srcBalance);
        Account expectedDstAccount = new Account(dstAccID, pin, dstBalance);
        accRepo.save(expectedSrcAccount);
        accRepo.save(expectedDstAccount);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void transferSelfTransferException(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        long dstAccID = srcAccID;
        long dstBalance = srcBalance;
        long amount = 10;
        String expectedMessage = "Source and destination accounts must be different.";
        int pin = 1111;

        Account expectedSrcAccount = new Account(srcAccID, pin, srcBalance);
        accRepo.save(expectedSrcAccount);

        SelfTransferException ex = Assertions.assertThrows(SelfTransferException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void getTransactionHistory() throws SQLException {
        long usrAccID = 1111L;
        long othAccID = 1112L;
        long amount = 10;
        int historyLimit = 10;
        int pin = 1111;
        int balance = 50;

        accRepo.save(new Account(usrAccID, pin, balance));
        accRepo.save(new Account(othAccID, pin, balance));

        Transaction otherTransaction = new Transaction(0, Transaction.Type.WITHDRAW,
                LocalDateTime.now(), amount, othAccID, null);

        //Set up test transactions in database
        List<Transaction> expectedTransactions = new ArrayList<>();
        expectedTransactions.add(new Transaction(0, Transaction.Type.DEPOSIT,
                LocalDateTime.now(), amount, null, usrAccID));
        sleep(1);
        expectedTransactions.add(new Transaction(0, Transaction.Type.WITHDRAW,
                LocalDateTime.now(), amount, usrAccID, null));
        sleep(1);
        expectedTransactions.add(otherTransaction);
        expectedTransactions.add(new Transaction(0, Transaction.Type.TRANSFER,
                LocalDateTime.now(), amount, usrAccID, othAccID));
        for(Transaction trans: expectedTransactions) {
            transRepo.save(trans);
        }
        expectedTransactions.remove(otherTransaction);
        expectedTransactions = expectedTransactions.reversed();

        List<Transaction> actualTransactions = transServ.getTransactionHistory(usrAccID, historyLimit);
        Assertions.assertEquals(expectedTransactions.size(), actualTransactions.size());
        for (int i = 0; i < expectedTransactions.size(); i++){
            Assertions.assertEquals(expectedTransactions.get(i), actualTransactions.get(i));
        }

    }

    @Test
    public void getTransactionHistoryLimit() throws SQLException {
        long usrAccID = 1111L;
        long othAccID = 1112L;
        long amount = 10;
        int historyLimit = 2;
        int pin = 1111;
        int balance = 50;

        accRepo.save(new Account(usrAccID, pin, balance));
        accRepo.save(new Account(othAccID, pin, balance));

        Transaction otherTransaction = new Transaction(0, Transaction.Type.WITHDRAW,
                LocalDateTime.now(), amount, othAccID, null);
        Transaction oldestTransaction = new Transaction(0, Transaction.Type.DEPOSIT,
                LocalDateTime.now(), amount, null, usrAccID);

        //Set up test transactions in database
        List<Transaction> expectedTransactions = new ArrayList<>();
        expectedTransactions.add(oldestTransaction);
        sleep(1);
        expectedTransactions.add(new Transaction(0, Transaction.Type.WITHDRAW,
                LocalDateTime.now(), amount, usrAccID, null));
        sleep(1);
        expectedTransactions.add(otherTransaction);
        expectedTransactions.add(new Transaction(0, Transaction.Type.TRANSFER,
                LocalDateTime.now(), amount, usrAccID, othAccID));
        for(Transaction trans: expectedTransactions) {
            transRepo.save(trans);
        }
        expectedTransactions.remove(otherTransaction);
        expectedTransactions.remove(oldestTransaction);
        expectedTransactions = expectedTransactions.reversed();

        List<Transaction> actualTransactions = transServ.getTransactionHistory(usrAccID, historyLimit);
        Assertions.assertEquals(expectedTransactions.size(), actualTransactions.size());
        for (int i = 0; i < expectedTransactions.size(); i++){
            Assertions.assertEquals(expectedTransactions.get(i), actualTransactions.get(i));
        }

    }

    @Test
    public void getTransactionHistoryNoHistory(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        int pin = 1111;
        String expectedMessage = "No transaction history found for account: " + srcAccID;

        Account expectedAccount = new Account(srcAccID, pin, srcBalance);
        accRepo.save(expectedAccount);

        NoTransactionHistoryException ex = Assertions.assertThrows(NoTransactionHistoryException.class,
                () -> transServ.getTransactionHistory(srcAccID, 10));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
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

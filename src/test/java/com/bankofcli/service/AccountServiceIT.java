package com.bankofcli.service;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InvalidPinException;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AccountServiceIT {

    public AccountService accServ;
    public AccountRepository accRep;

    @BeforeEach
    public void setup(){
        accRep = new AccountRepository();
        accServ = new AccountService(accRep);
    }

    @AfterEach
    public void teardown(){
        DatabaseManager db = new DatabaseManager();
        String deleteQry = "DELETE FROM accounts";
        try(Connection con = db.open();
            PreparedStatement ps = con.prepareStatement(deleteQry)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registerPositive(){
        int pin = 1111;
        long balance = 0;
        Account expectedAccount = new Account(1, pin, balance);

        //Attempt to register new account
        Account resultAccount = accServ.register(pin);

        //Check if new Account has been successfully saved to database.
        Assertions.assertNotNull(accRep.findByID(resultAccount.getAccountID()));
        Assertions.assertEquals(expectedAccount.getPin(), resultAccount.getPin());
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void registerPositiveZeroPin(){
        int pin = 0;
        int balance = 0;
        Account expectedAccount = new Account(1,pin, balance);

        Account resultAccount = accServ.register(pin);

        //Check if new Account has been successfully saved to database.
        Assertions.assertNotNull(accRep.findByID(resultAccount.getAccountID()));
        Assertions.assertEquals(expectedAccount.getPin(), resultAccount.getPin());
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void loginPositive(){
        long accID = 11111L;
        int pin = 1111;

        Account expectedAccount = new Account(accID, pin, 0);
        accRep.save(expectedAccount);

        Account resultAccount = accServ.login(accID, pin);

        Assertions.assertEquals(expectedAccount, resultAccount);
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }



    @Test
    public void loginAccountNotFoundException(){
        long accID = 1111L;
        int pin = 1111;
        String expectedMessage = "Account not found.";

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accServ.login(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void loginInvalidPinException(){
        long accID = 11111L;
        int pin1 = 1234;
        int pin2 = 1235;
        long balance = 1000; //$10.00
        String expectedMessage = "Incorrect PIN.";

        Account expectedAccount = new Account(accID, pin1, balance);
        accRep.save(expectedAccount);

        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.login(accID, pin2));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void getBalancePositive(){
        long accID = 11111L;
        long balance = 1000; //$10.00
        int pin = 1111;
        Account expectedAccount = new Account(accID, pin, balance);

        accRep.save(expectedAccount);
        Assertions.assertEquals(accServ.getBalance(accID), balance);
    }

    @Test
    public void getBalanceAccountNotFoundException(){
        long accID = 11111L;
        String expectedMessage = "Account not found.";

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accServ.getBalance(accID));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

}

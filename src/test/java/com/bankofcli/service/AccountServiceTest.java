package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AccountServiceTest {

    public AccountService accServ;
    public AccountRepository accRep;

    @BeforeEach
    public void setup(){
        accRep = Mockito.mock(AccountRepository.class);
        accServ = new AccountService(accRep);
    }

    @Test
    public void registerPositive(){
        long accID = 11111L;
        int pin = 1111;
        Account expectedAccount = new Account(accID, pin, 0);
        Mockito.when(accRep.findByID(accID)).thenReturn(null);
        Account resultAccount = accServ.register(accID, pin);
        Assertions.assertEquals(expectedAccount, resultAccount);
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void registerPositiveZeroPin(){
        long accID = 11111L;
        int pin = 1111;
        Account expectedAccount = new Account(accID, pin, 0);
        Mockito.when(accRep.findByID(accID)).thenReturn(null);
        Account resultAccount = accServ.register(accID, pin);
        Assertions.assertEquals(expectedAccount, resultAccount);
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void registerDuplicateExistsException(){
        long accID = 11111L;
        int pin = 1111;
        String expectedMessage = "Account ID already exists";
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, pin, 0));
        DuplicateAccountException ex =
                Assertions.assertThrows(DuplicateAccountException.class,
                        () -> accServ.register(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void registerInvalidAccountIDExceptionZero(){
        long accID = 0;
        int pin = 1111;
        String expectedMessage = "Account ID must be positive.";
        InvalidAccountIDException ex = Assertions.assertThrows(InvalidAccountIDException.class,
                () -> accServ.register(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void registerInvalidAccountIDExceptionNegative(){
        long accID = -1;
        int pin = 1111;
        String expectedMessage = "Account ID must be positive.";
        InvalidAccountIDException ex = Assertions.assertThrows(InvalidAccountIDException.class,
                () -> accServ.register(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void registerInvalidPinExceptionNegative(){
        long accID = 11111L;
        int pin = -1;
        String expectedMessage = "PIN must be 4 digits.";
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.register(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void registerInvalidPinExceptionTooLarge(){
        long accID = 11111L;
        int pin = 10000;
        String expectedMessage = "PIN must be 4 digits.";
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.register(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void loginPositive(){
        long accID = 11111L;
        int pin = 1111;
        int balance = 1000; //$10.00
        Account expectedAccount = new Account(accID, pin, balance);
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, pin, balance));
        Account resultAccount = accServ.login(accID, pin);
        Assertions.assertEquals(expectedAccount, resultAccount);
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void loginAccountNotFoundException(){
        long accID = 11111L;
        int pin = 1111;
        int balance = 1000; //$10.00
        String expectedMessage = "Account not found.";
        Mockito.when(accRep.findByID(accID)).thenReturn(null);
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accServ.login(accID, pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void loginInvalidPinException(){
        long accID = 11111L;
        int pin1 = 1234;
        int pin2 = 1235;
        int balance = 1000; //$10.00
        String expectedMessage = "Incorrect PIN.";
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, pin1, balance));
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.login(accID, pin2));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void getBalancePositive(){
        long accID = 11111L;
        int balance = 1000; //$10.00
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, 1111, balance));
        Assertions.assertEquals(accServ.getBalance(accID), balance);
    }

    @Test
    public void getBalanceAccountNotFoundException(){
        long accID = 11111L;
        int balance = 1000; //$10.00
        String expectedMessage = "Account not found.";
        Mockito.when(accRep.findByID(accID)).thenReturn(null);
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accServ.getBalance(accID));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

}

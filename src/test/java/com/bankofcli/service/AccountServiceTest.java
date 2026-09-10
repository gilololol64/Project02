package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

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
        int pin = 1111;
        Account expectedAccount = new Account(1, pin, 0);
        Mockito.when(accRep.findByID(anyLong())).thenReturn(null);
        Account resultAccount = accServ.register(pin);
        Assertions.assertEquals(expectedAccount.getPin(), resultAccount.getPin());
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void registerPositiveZeroPin(){
        int pin = 0;
        Account expectedAccount = new Account(1,pin, 0);
        Mockito.when(accRep.findByID(anyLong())).thenReturn(null);
        Account resultAccount = accServ.register(pin);
        Assertions.assertEquals(expectedAccount.getPin(), resultAccount.getPin());
        Assertions.assertEquals(expectedAccount.getBalanceExtendedCents(), resultAccount.getBalanceExtendedCents());
    }

    @Test
    public void registerInvalidPinExceptionNegative(){
        long accID = 11111L;
        int pin = -1;
        String expectedMessage = "PIN must be 4 digits.";
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.register(pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void registerInvalidPinExceptionTooLarge(){
        int pin = 10000;
        String expectedMessage = "PIN must be 4 digits.";
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.register(pin));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void loginPositive(){
        long accID = 11111L;
        int pin = 1111;
        long balance = 1000; //$10.00
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
        long balance = 1000; //$10.00
        String expectedMessage = "Incorrect PIN.";
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, pin1, balance));
        InvalidPinException ex = Assertions.assertThrows(InvalidPinException.class,
                () -> accServ.login(accID, pin2));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void getBalancePositive(){
        long accID = 11111L;
        long balance = 1000; //$10.00
        Mockito.when(accRep.findByID(accID)).thenReturn(new Account(accID, 1111, balance));
        Assertions.assertEquals(accServ.getBalance(accID), balance);
    }

    @Test
    public void getBalanceAccountNotFoundException(){
        long accID = 11111L;
        String expectedMessage = "Account not found.";
        Mockito.when(accRep.findByID(accID)).thenReturn(null);
        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () -> accServ.getBalance(accID));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

}

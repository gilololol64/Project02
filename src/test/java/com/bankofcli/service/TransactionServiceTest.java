package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class TransactionServiceTest {

    public TransactionService transServ;
    public AccountRepository accRepo;
    public TransactionRepository transRepo;
    public Account accSrc;

    @BeforeEach
    public void setup(){
        accRepo = Mockito.mock(AccountRepository.class);
        transRepo = Mockito.mock(TransactionRepository.class);
        transServ = new TransactionService(accRepo, transRepo);
    }

    @Test
    public void depositPositive(){
        long accID = 1111L;
        long balance = 0;
        long deposit = 2500;
        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        transServ.deposit(accID, deposit);

        verify(accRepo, times(1)).save(mockAcc);
        verify(transRepo, times(1)).save(any());
    }

    @Test
    public void depositNegativeAmount(){
        long accID = 1111L;
        long balance = 0;
        long deposit = -100;
        String expectedMessage = "Transaction amount can not be less than zero.";

        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void depositMaxAmount(){
        long accID = 1111L;
        long balance = 1;
        long deposit = Long.MAX_VALUE;
        String expectedMessage = "Transaction amount too large to process";

        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void depositAccountNotFoundException(){
        long accID = 1111L;
        long deposit = 100;
        String expectedMessage = "Account " + accID + " was not found.";

        Mockito.when(accRepo.findByID(accID)).thenReturn(null);

        AccountNotFoundException ex = Assertions.assertThrows(AccountNotFoundException.class,
                () ->transServ.deposit(accID, deposit));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawPositive(){
        long accID = 1111L;
        long balance = 2500;
        long withdraw = 2500;
        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        transServ.withdraw(accID, withdraw);

        verify(accRepo, times(1)).save(mockAcc);
        verify(transRepo, times(1)).save(any());
    }

    @Test
    public void withdrawInsufficientFundsException(){
        long accID = 1111L;
        long balance = 2500;
        long withdraw = 2501;
        String expectedMessage = "Insufficient funds.";

        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        InsufficientFundsException ex = Assertions.assertThrows(InsufficientFundsException.class,
                () ->transServ.withdraw(accID, withdraw));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawNegativeAmount(){
        long accID = 1111L;
        long balance = 0;
        long withdraw = -100;
        String expectedMessage = "Transaction amount can not be less than zero.";

        Account mockAcc = new Account(accID, 1111, balance);
        Mockito.when(accRepo.findByID(accID)).thenReturn(mockAcc);

        InvalidAmountException ex = Assertions.assertThrows(InvalidAmountException.class,
                () ->transServ.withdraw(accID, withdraw));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    public void withdrawAccountNotFoundException(){
        long accID = 1111L;
        long withdraw = 100;
        String expectedMessage = "Account " + accID + " was not found.";

        Mockito.when(accRepo.findByID(accID)).thenReturn(null);

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
        long amount = 0;

        Account mockSrcAcc = new Account(srcAccID, 1111, srcBalance);
        Account mockDstAcc = new Account(dstAccID, 1111, dstBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(mockSrcAcc);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(mockDstAcc);

        transServ.transfer(srcAccID, dstAccID, amount);

        verify(accRepo, times(1)).save((List<Account>) any());
        verify(transRepo, times(1)).save(any());
    }


    @Test
    public void transferInsufficientFundsException(){
        long srcAccID = 1111L;
        long srcBalance = 0;
        long dstAccID = 1112L;
        long dstBalance = 0;
        long amount = 10;
        String expectedMessage = "Insufficient funds.";

        Account mockSrcAcc = new Account(srcAccID, 1111, srcBalance);
        Account mockDstAcc = new Account(dstAccID, 1111, dstBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(mockSrcAcc);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(mockDstAcc);

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

        Account mockDstAcc = new Account(dstAccID, 1111, dstBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(null);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(mockDstAcc);

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

        Account mockSrcAcc = new Account(srcAccID, 1111, srcBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(mockSrcAcc);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(null);

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
        String expectedMessage = "Transaction amount can not be less than zero.";

        Account mockSrcAcc = new Account(srcAccID, 1111, srcBalance);
        Account mockDstAcc = new Account(dstAccID, 1111, dstBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(mockSrcAcc);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(mockDstAcc);

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

        Account mockSrcAcc = new Account(srcAccID, 1111, srcBalance);
        Account mockDstAcc = new Account(dstAccID, 1111, dstBalance);
        Mockito.when(accRepo.findByID(srcAccID)).thenReturn(mockSrcAcc);
        Mockito.when(accRepo.findByID(dstAccID)).thenReturn(mockDstAcc);

        SelfTransferException ex = Assertions.assertThrows(SelfTransferException.class,
                () -> transServ.transfer(srcAccID, dstAccID, amount));
        Assertions.assertEquals(expectedMessage, ex.getMessage());
    }

}

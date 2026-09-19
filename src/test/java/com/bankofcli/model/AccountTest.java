package com.bankofcli.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTest {

    @Test
    public void getBalancePositive(){
        int balance = 1000; //10.00
        double expectedBalance = 10.00;
        Account account = new Account(-1L, "", balance);
        Assertions.assertEquals(expectedBalance, account.getBalance());
    }
}

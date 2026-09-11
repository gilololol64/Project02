package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import java.security.SecureRandom;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Registers a new account
    public Account register(int pin) {

        if (!isValidPin(pin)) {
            throw new InvalidPinException("PIN must be 4 digits.");
        }

        SecureRandom random = new SecureRandom();
        Account existingAccount;
        long accountID;

        do {
            accountID = generateNewAccountID(random);
            existingAccount = accountRepository.findByID(accountID);
        }while(existingAccount != null);

        Account newAccount = new Account(accountID, pin, 0);

        accountRepository.save(newAccount);

        return newAccount;
    }

    // Logs a user into an existing account
    public Account login(long accountID, int pin) {

        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            throw new AccountNotFoundException("Account not found.");
        }

        if (account.getPin() != pin) {
            throw new InvalidPinException("Incorrect PIN.");
        }

        return account;
    }

    // Returns the current account balance in extended cents
    public long getBalance(long accountID) {
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            throw new AccountNotFoundException("Account not found.");
        }

        return account.getBalanceExtendedCents();
    }

    // Checks that the pin contains exactly four digits
    private boolean isValidPin(int pin) {
        return pin >= 0 & pin <= 9999;
    }

    //Generates a random (up to 10 digit) account number given a secure random object
    private long generateNewAccountID(SecureRandom random){
        random.setSeed(System.currentTimeMillis()); //Uses the current time as a seed
        long min = 1L;
        long max = 9999999999L; //10 digit value for account number

        return random.nextLong(min, max);
    }
}

package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {

    // Dedicated error logger - name must match a <logger> element in logback.xml
    // to route to the general error log file instead of falling through to root.
    private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");

    // General action logger - name doesn't matter, inherits from root and
    // lands in the AccountAction log file. Only ever used for .info() calls.
    private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Registers a new account
    public Account register(int pin) {

        if (!isValidPin(pin)) {
            errorLogger.error("Registration failed, PIN did not meet format requirements.");
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

        actionLogger.info("Account {} successfully registered.", accountID);

        return newAccount;
    }

    // Logs a user into an existing account
    public Account login(long accountID, int pin) {

        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            errorLogger.error("Login failed, account ID {} not found.", accountID);
            throw new AccountNotFoundException("Account not found.");
        }

        if (account.getPin() != pin) {
            errorLogger.error("Login failed for account {}, incorrect PIN entered.", accountID);
            throw new InvalidPinException("Incorrect PIN.");
        }

        actionLogger.info("Account {} successfully logged in.", accountID);

        return account;
    }

    // Returns the current account balance in extended cents
    public long getBalance(long accountID) {
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            errorLogger.error("Balance lookup failed, account ID {} not found.", accountID);
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

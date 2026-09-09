package com.bankofcli.service;

import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Registers a new account
    public Account register(long accountID, int pin) {

        if (accountID <= 0) {
            logger.error("Registration failed, invalid account ID {}.", accountID);
            throw new InvalidAccountIDException("Account ID must be positive.");
        }

        if (!isValidPin(pin)) {
            logger.error("Registration failed for account ID {}, PIN did not meet format requirements.", accountID);
            throw new InvalidPinException("PIN must be 4 digits.");
        }

        Account existingAccount = accountRepository.findByID(accountID);

        if (existingAccount != null) {
            logger.error("Registration failed, account ID {} already exists.", accountID);
            throw new DuplicateAccountException("Account ID already exists");
        }

        Account newAccount = new Account(accountID, pin, 0);

        accountRepository.save(newAccount);

        logger.info("Account {} successfully registered.", accountID);

        return newAccount;
    }

    // Logs a user into an existing account
    public Account login(long accountID, int pin) {

        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            logger.error("Login failed, account ID {} not found.", accountID);
            throw new AccountNotFoundException("Account not found.");
        }

        if (account.getPin() != pin) {
            logger.error("Login failed for account {}, incorrect PIN entered.", accountID);
            throw new InvalidPinException("Incorrect PIN.");
        }

        logger.info("Account {} successfully logged in.", accountID);

        return account;
    }

    // Returns the current account balance in extended cents
    public int getBalance(long accountID) {
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            logger.error("Balance lookup failed, account ID {} not found.", accountID);
            throw new AccountNotFoundException("Account not found.");
        }

        return account.getBalanceExtendedCents();
    }

    // Checks that the pin contains exactly four digits
    private boolean isValidPin(int pin) {
        return pin >= 1000 & pin <= 9999;
    }
}

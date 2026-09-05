package com.bankofcli.service;

import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Registers a new account
    public Account register(long accountID, int pin) {

        if (accountID <= 0) {
            throw new IllegalArgumentException("Account ID must be positive.");
        }

        if (!isValidPin(pin)) {
            throw new IllegalArgumentException("PIN must be 4 digits.");
        }

        Account existingAccount = accountRepository.findByID(accountID);

        if (existingAccount != null) {
            throw new IllegalArgumentException("Account ID already exists");
        }

        Account newAccount = new Account(accountID, pin, 0);

        accountRepository.save(newAccount);

        return newAccount;
    }

    // Logs a user into an existing account
    public Account login(long accountID, int pin) {

        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        if (account.getPin() != pin) {
            throw new IllegalArgumentException("Incorrect PIN.");
        }

        return account;
    }

    // Returns the current account balance in extended cents
    public int getBalance(long accountID) {
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        return account.getBalanceExtendedCents();
    }

    // Checks that the pin contains exactly four digits
    private boolean isValidPin(int pin) {
        return pin >= 1000 & pin <= 9999;
    }
}

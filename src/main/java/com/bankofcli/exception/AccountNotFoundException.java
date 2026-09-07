package com.bankofcli.exception;

/**
 * Thrown when an account ID doesn't match any account in the system.
 * Covers logging in with an unknown ID and a transfer aimed at an account
 * that doesn't exist.
 */
public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}

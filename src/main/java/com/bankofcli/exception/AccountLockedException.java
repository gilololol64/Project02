package com.bankofcli.exception;

/**
 * Thrown when a user enters the incorrect PIN three times consecutively. Locks the 
 * account for 5 minutes. A successful login resets the failed-attempt counter.
 */
public class AccountLockedException extends BankException {

    public AccountLockedException(String message) {
        super(message);
    }
}
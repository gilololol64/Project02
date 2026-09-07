package com.bankofcli.exception;

/**
 * Thrown during registration when the chosen account ID is already taken.
 */
public class DuplicateAccountException extends BankException {

    public DuplicateAccountException(String message) {
        super(message);
    }
}

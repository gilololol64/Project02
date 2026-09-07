package com.bankofcli.exception;

/**
 * Thrown when a login attempt uses the wrong PIN for an account that does exist.
 * Kept separate from AccountNotFoundException so a failed login gets logged
 * as a security event rather than a missing-account lookup.
 */
public class InvalidPinException extends BankException {

    public InvalidPinException(String message) {
        super(message);
    }
}

package com.bankofcli.exception;

/**
 * Thrown when something below the service layer fails in a way the user
 * shouldn't see directly, e.g. a lost database connection or a corrupt row.
 * The real technical error should be logged using the cause passed in here;
 * the message shown to the user should stay generic (something like
 * "Service temporarily unavailable, please try again later").
 */
public class ServiceUnavailableException extends BankException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.bankofcli.exception;

/**
 * Base class for every custom exception thrown by the Bank of CLI application.
 * Letting BankCLI catch this one type means every user-facing error message
 * is guaranteed to be something we wrote on purpose, not a raw stack trace.
 */
public abstract class BankException extends RuntimeException {

    /**
     * @param message A short, user-friendly explanation of what went wrong
     */
    public BankException(String message) {
        super(message);
    }

    /**
     * @param message A short, user-friendly explanation of what went wrong
     * @param cause The underlying exception that triggered this one, kept around for logging
     */
    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}

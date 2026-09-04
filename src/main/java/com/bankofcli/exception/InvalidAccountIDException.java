package com.bankofcli.exception;

/**
 * Thrown when an invalid Account ID is given such as a value less than 1 or a String
 */
public class InvalidAccountIDException extends RuntimeException {
    public InvalidAccountIDException(String message) {
        super(message);
    }
}

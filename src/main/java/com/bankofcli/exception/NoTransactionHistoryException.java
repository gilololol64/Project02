package com.bankofcli.exception;

public class NoTransactionHistoryException extends RuntimeException {
    public NoTransactionHistoryException(String message) {
        super(message);
    }
}

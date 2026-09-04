package com.bankofcli.exception;

/**
 * Thrown when a withdrawal or transfer would take an account's balance below zero.
 * The account exists and the PIN is correct, it just can't cover the amount.
 */
public class InsufficientFundsException extends BankException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}

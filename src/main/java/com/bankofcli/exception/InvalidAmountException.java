package com.bankofcli.exception;

/**
 * Thrown when a deposit, withdraw, or transfer amount fails validation.
 * Covers negative amounts, fractional cents (ie. $3.131), and non-numeric input.
 */
public class InvalidAmountException extends BankException {

    public InvalidAmountException(String message) {
        super(message);
    }
}

package com.bankofcli.exception;

/**
 * Thrown when a transfer's source and destination account are the same account.
 * Not an amount problem and not a missing account, so it gets its own type
 * rather than being folded into InvalidAmountException.
 */
public class SelfTransferException extends BankException {

    public SelfTransferException(String message) {
        super(message);
    }
}

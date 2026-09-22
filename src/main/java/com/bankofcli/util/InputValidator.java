package com.bankofcli.util;

import com.bankofcli.exception.InvalidAccountIDException;
import com.bankofcli.exception.InvalidAmountException;
import com.bankofcli.exception.InvalidPinException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralizes parsing and format validation of raw user input strings before they
 * reach the Service layer. Only checks that input is shaped correctly (is it a number,
 * right length, etc). Business rules that depend on account state, like whether a PIN
 * actually matches an account or whether there's enough balance, stay in the Service
 * layer where they belong.
 */
public final class InputValidator {

    private InputValidator() {
        // Utility class, not meant to be instantiated.
    }

    /**
     * Parses a raw account ID string into a positive whole number.
     * @param input raw text from an Account ID field
     * @return the parsed account ID
     * @throws InvalidAccountIDException if input isn't a positive whole number
     */
    public static long parseAccountId(String input) {
        long accountID;
        try {
            accountID = Long.parseLong(input.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidAccountIDException("Please enter a valid Account ID.");
        }
        if (accountID <= 0) {
            throw new InvalidAccountIDException("Account ID must be a positive number.");
        }
        return accountID;
    }

    /**
     * Parses a raw PIN string and confirms it's exactly four digits.
     * @param input raw text from a PIN field
     * @return the parsed PIN
     * @throws InvalidPinException if input isn't parseable or isn't 4 digits
     */
    public static int parsePin(String input) {
        int pin;
        try {
            pin = Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidPinException("PIN must be 4 digits.");
        }
        if (!isValidPinFormat(pin)) {
            throw new InvalidPinException("PIN must be 4 digits.");
        }
        return pin;
    }

    /**
     * Checks whether an already-parsed PIN is exactly four digits. Shared by parsePin()
     * above and by AccountService, so the format rule only lives in one place.
     * @param pin the PIN to check
     * @return true if the PIN is between 1000 and 9999 inclusive
     */
    public static boolean isValidPinFormat(int pin) {
        return pin >= 1000 && pin <= 9999;
    }

    /**
     * Parses a raw dollar amount string into extended cents format ($10.00 = 1000L),
     * rejecting anything with more than two decimal places.
     * @param input raw text from an amount field
     * @return the parsed amount in extended cents
     * @throws InvalidAmountException if input isn't a valid amount with at most two decimal places
     */
    public static long parseAmount(String input) {
        try {
            BigDecimal dollars = new BigDecimal(input.trim()).setScale(2, RoundingMode.UNNECESSARY);
            return dollars.movePointRight(2).longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new InvalidAmountException(
                    "Please enter a valid amount with no more than two decimal places.");
        }
    }
}

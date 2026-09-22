package com.bankofcli.util;

import com.bankofcli.exception.InvalidAccountIDException;
import com.bankofcli.exception.InvalidAmountException;
import com.bankofcli.exception.InvalidPinException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    // --- parseAccountId ---

    @Test
    public void parseAccountIdValid() {
        assertEquals(12345L, InputValidator.parseAccountId("12345"));
    }

    @Test
    public void parseAccountIdRejectsBlank() {
        assertThrows(InvalidAccountIDException.class, () -> InputValidator.parseAccountId(""));
    }

    @Test
    public void parseAccountIdRejectsLetters() {
        assertThrows(InvalidAccountIDException.class, () -> InputValidator.parseAccountId("abc123"));
    }

    @Test
    public void parseAccountIdRejectsZero() {
        assertThrows(InvalidAccountIDException.class, () -> InputValidator.parseAccountId("0"));
    }

    @Test
    public void parseAccountIdRejectsNegative() {
        assertThrows(InvalidAccountIDException.class, () -> InputValidator.parseAccountId("-5"));
    }

    // --- parsePin ---

    @Test
    public void parsePinValid() {
        assertEquals(1234, InputValidator.parsePin("1234"));
    }

    @Test
    public void parsePinPreservesLeadingZeros() {
        // Regression test - "0102" used to parse to int 102 and get rejected
        // as too short, since the old check ran after parsing to int.
        assertEquals(102, InputValidator.parsePin("0102"));
        assertEquals(0, InputValidator.parsePin("0000"));
    }

    @Test
    public void parsePinRejectsThreeDigits() {
        assertThrows(InvalidPinException.class, () -> InputValidator.parsePin("123"));
    }

    @Test
    public void parsePinRejectsFiveDigits() {
        assertThrows(InvalidPinException.class, () -> InputValidator.parsePin("12345"));
    }

    @Test
    public void parsePinRejectsLetters() {
        assertThrows(InvalidPinException.class, () -> InputValidator.parsePin("abcd"));
    }

    @Test
    public void parsePinRejectsBlank() {
        assertThrows(InvalidPinException.class, () -> InputValidator.parsePin(""));
    }

    @Test
    public void parsePinRejectsNegativeSign() {
        // "-123" is 4 characters but not 4 digits, must be rejected.
        assertThrows(InvalidPinException.class, () -> InputValidator.parsePin("-123"));
    }

    // --- isValidPinFormat ---

    @Test
    public void isValidPinFormatAcceptsFullRange() {
        assertTrue(InputValidator.isValidPinFormat(0));
        assertTrue(InputValidator.isValidPinFormat(102));
        assertTrue(InputValidator.isValidPinFormat(9999));
    }

    @Test
    public void isValidPinFormatRejectsOutOfRange() {
        assertFalse(InputValidator.isValidPinFormat(-1));
        assertFalse(InputValidator.isValidPinFormat(10000));
    }

    // --- parseAmount ---

    @Test
    public void parseAmountValid() {
        assertEquals(1050L, InputValidator.parseAmount("10.50"));
    }

    @Test
    public void parseAmountValidWholeNumber() {
        assertEquals(1000L, InputValidator.parseAmount("10"));
    }

    @Test
    public void parseAmountRejectsThreeDecimalPlaces() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.parseAmount("10.999"));
    }

    @Test
    public void parseAmountRejectsLetters() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.parseAmount("abc"));
    }

    @Test
    public void parseAmountRejectsBlank() {
        assertThrows(InvalidAmountException.class, () -> InputValidator.parseAmount(""));
    }
}

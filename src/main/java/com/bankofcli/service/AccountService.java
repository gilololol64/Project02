package com.bankofcli.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.bankofcli.exception.*;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    // Dedicated error logger - name must match a <logger> element in logback.xml
    // to route to the general error log file instead of falling through to root.
    private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");

    // General action logger - name doesn't matter, inherits from root and
    // lands in the AccountAction log file. Only ever used for .info() calls.
    private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

    private final AccountRepository accountRepository;

    private final Map<Long, Integer> failedAttempts = new HashMap<>();
    private final Map<Long, LocalDateTime> lockedUntil = new HashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 5;
    private static final int COST_FACTOR = 10;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Registers a new account
    public Account register(int pin) {

        actionLogger.info("Attempting to register new account");

        if (!isValidPin(pin)) {
            InvalidPinException ex = new InvalidPinException("PIN must be 4 digits.");
            errorLogger.error("Registration failed, PIN did not meet format requirements.", ex);
            throw ex;
        }

        SecureRandom random = new SecureRandom();
        Account existingAccount;
        long accountID;

        do {
            accountID = generateNewAccountID(random);
            existingAccount = accountRepository.findByID(accountID);
        }while(existingAccount != null);

        String pinHash = hashPin(pin);

        Account newAccount = new Account(accountID, pinHash, 0);

        accountRepository.save(newAccount);

        actionLogger.info("Account {} successfully registered.", accountID);

        return newAccount;
    }

    // Logs a user into an existing account
    public Account login(long accountID, int pin) {

        actionLogger.info("Attempting to login Account {}", accountID);
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            AccountNotFoundException ex = new AccountNotFoundException("Account not found.");
            errorLogger.error("Login failed, account ID {} not found.", accountID, ex);
            throw ex;
        }

        // Check if the account is currently locked
        LocalDateTime lockExpiration = lockedUntil.get(accountID);

        if (lockExpiration != null) {

            // Account is still locked
            if (LocalDateTime.now().isBefore(lockExpiration)) {
                errorLogger.error(
                    "Login failed for account {}, account is temporarily locked.",
                    accountID
                );

                throw new AccountLockedException(
                    "Account is temporarily locked. Please try again later."
                );
            }

            // Lockout time has expired, so reset the account
            lockedUntil.remove(accountID);
            failedAttempts.remove(accountID);
        }

        // Check PIN
        if (!verifyPinHash(pin, account.getPinHash())) {

            int attempts = failedAttempts.getOrDefault(accountID, 0) + 1;
            failedAttempts.put(accountID, attempts);

            // Lock account after third incorrect attempt
            if (attempts >= MAX_ATTEMPTS) {
                lockedUntil.put(
                    accountID,
                    LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES)
                );

                errorLogger.error("Account {} locked after {} incorrect PIN attempts.",
                    accountID,
                    attempts
                );

                throw new AccountLockedException(
                    "Too many incorrect PIN attempts. Account locked for 5 minutes."
                );
            }

            int attemptsRemaining = MAX_ATTEMPTS - attempts;

            errorLogger.error(
                "Login failed for account {}, incorrect PIN entered. {} attempt(s) remaining.",
                accountID,
                attemptsRemaining
            );

            throw new InvalidPinException("Incorrect PIN. " + attemptsRemaining + " attempt(s) remaining.");
        }

        // Correct PIN - reset failed attempts
        failedAttempts.remove(accountID);

        actionLogger.info("Account {} successfully logged in.", accountID);

        return account;
    }

    // Returns the current account balance in extended cents
    public long getBalance(long accountID) {
        actionLogger.info("Attempting to get balance of Account {}.", accountID);
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            AccountNotFoundException ex = new AccountNotFoundException("Account not found.");
            errorLogger.error("Balance lookup failed, account ID {} not found.", accountID, ex);
            throw ex;
        }

        actionLogger.info("Balance of Account {} successfully retrieved.", accountID);
        return account.getBalanceExtendedCents();
    }

    // Changes an account's PIN after verifying the current one
    public void changePin(long accountID, int currentPin, int newPin) {

        actionLogger.info("Attempting to change PIN for Account {}.", accountID);
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            AccountNotFoundException ex = new AccountNotFoundException("Account not found.");
            errorLogger.error("PIN change failed, account ID {} not found.", accountID, ex);
            throw ex;
        }

        if (!verifyPinHash(currentPin, account.getPinHash())) {
            InvalidPinException ex = new InvalidPinException("Current PIN is incorrect.");
            errorLogger.error("PIN change failed for account {}, current PIN entered was incorrect.", accountID, ex);
            throw ex;
        }

        if (!isValidPin(newPin)) {
            InvalidPinException ex = new InvalidPinException("New PIN must be 4 digits.");
            errorLogger.error("PIN change failed for account {}, new PIN did not meet format requirements.", accountID, ex);
            throw ex;
        }

        if (verifyPinHash(newPin, account.getPinHash())) {
            InvalidPinException ex = new InvalidPinException("New PIN must be different from current PIN.");
            errorLogger.error("PIN change failed for account {}, new PIN matched current PIN.", accountID, ex);
            throw ex;
        }

        account.setPinHash(hashPin(newPin));
        accountRepository.save(account);

        actionLogger.info("PIN successfully changed for account {}.", accountID);
    }

    // Checks that the pin contains exactly four digits
    private boolean isValidPin(int pin) {
        return pin >= 0 & pin <= 9999;
    }

    //Generates a random (up to 10 digit) account number given a secure random object
    private long generateNewAccountID(SecureRandom random){
        random.setSeed(System.currentTimeMillis()); //Uses the current time as a seed
        long min = 1L;
        long max = 9999999999L; //10 digit value for account number

        return random.nextLong(min, max);
    }

    public static String hashPin(int pin){
        char [] pinCharArray = Integer.toString(pin).toCharArray();

        String pinHash = BCrypt.withDefaults().hashToString(COST_FACTOR, pinCharArray);
        return pinHash;
    }

    public static boolean verifyPinHash(int userInput, String pinHash){

        char [] inputPinCharArray = Integer.toString(userInput).toCharArray();

        BCrypt.Result result = BCrypt.verifyer().verify(inputPinCharArray, pinHash);
        return result.verified;
    }

}

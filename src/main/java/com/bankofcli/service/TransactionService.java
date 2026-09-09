package com.bankofcli.service;

import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.model.Transaction.Type;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;

import com.bankofcli.exception.*;

import java.time.LocalDateTime;

public class TransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Deposits money into an account
    public Transaction deposit(long accountID, long amount) {
        validateAmount(amount);

        Account account = getAccountOrThrow(accountID);

        long newBalance = 0;
        try {
            newBalance = Math.addExact(account.getBalanceExtendedCents(), amount);
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("Transaction amount too large to process");
        }

        account.setBalanceExtendedCents(newBalance);
        accountRepository.update(account);

        Transaction transaction = new Transaction(
            0,
            Type.DEPOSIT,
            LocalDateTime.now(),
            amount,
            null,
            accountID
        );

        transactionRepository.save(transaction);

        return transaction;
    }

    // Withdraws money from an account
    public Transaction withdraw(long accountID, long amount) {

        validateAmount(amount);

        Account account = getAccountOrThrow(accountID);

        if (account.getBalanceExtendedCents() < amount) {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        long newBalance = account.getBalanceExtendedCents() - amount;

        account.setBalanceExtendedCents(newBalance);
        accountRepository.update(account);

        Transaction transaction = new Transaction(
            0,
            Type.WITHDRAW,
            LocalDateTime.now(),
            amount,
            accountID,
            null
        );

        transactionRepository.save(transaction);

        return transaction;
    }

    // Transfers money from one account to another
    public Transaction transfer(long sourceAccountID, long destinationAccountID, long amount) {
        
        validateAmount(amount);

        if (sourceAccountID == destinationAccountID) {
            throw new SelfTransferException("Source and destination accounts must be different.");
        }

        Account source = getAccountOrThrow(sourceAccountID);
        Account destination = getAccountOrThrow(destinationAccountID);

        if (source.getBalanceExtendedCents() < amount) {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        long destinationBalance = 0;
        try {
            destinationBalance = Math.addExact(destination.getBalanceExtendedCents(), amount);
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("Transaction amount too large to process");
        }

        source.setBalanceExtendedCents(source.getBalanceExtendedCents() - amount);

        destination.setBalanceExtendedCents(destinationBalance);

        accountRepository.update(source);
        accountRepository.update(destination);

        Transaction transaction = new Transaction(
            0,
            Type.TRANSFER,
            LocalDateTime.now(),
            amount,
            sourceAccountID,
            destinationAccountID
        );

        transactionRepository.save(transaction);

        return transaction;
    }

    // Ensures transaction amount is valid
    private void validateAmount(long amount) {
        if (amount < 0) {
            throw new InvalidAmountException("Transaction amount can not be less than zero.");

        }
    }

    private Account getAccountOrThrow(long accountID) {
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            throw new AccountNotFoundException("Account " + accountID + " was not found.");
        }
        return account;
    }
}

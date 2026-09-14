package com.bankofcli.service;

import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.model.Transaction.Type;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;

import com.bankofcli.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Dedicated error logger - name must match a <logger> element in logback.xml
    // to route to the general error log file instead of falling through to root.
    private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");

    // General action logger - name doesn't matter, inherits from root and
    // lands in the AccountAction log file. Only ever used for .info() calls.
    private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Deposits money into an account
    public Transaction deposit(long accountID, long amount) {
        actionLogger.info("Attempting to deposit ${} into Account {}",
                String.format("%,.2f",amount / 100.0), accountID);
        validateAmount(amount);

        Account account = getAccountOrThrow(accountID);

        long newBalance = 0;
        try {
            newBalance = Math.addExact(account.getBalanceExtendedCents(), amount);
        } catch (ArithmeticException e) {
            InvalidAmountException ex = new InvalidAmountException("Transaction amount too large to process");
            errorLogger.error("Amount to large to process as a long.", ex);
            throw ex;
        }

        account.setBalanceExtendedCents(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction(
            0,
            Type.DEPOSIT,
            LocalDateTime.now(),
            amount,
            null,
            accountID
        );

        transactionRepository.save(transaction);

        actionLogger.info("Deposit successfully made.");
        return transaction;
    }

    // Withdraws money from an account
    public Transaction withdraw(long accountID, long amount) {
        actionLogger.info("Attempting to withdraw ${} into Account {}",
                String.format("%,.2f",amount / 100.0), accountID);
        validateAmount(amount);

        Account account = getAccountOrThrow(accountID);

        if (account.getBalanceExtendedCents() < amount) {
            InsufficientFundsException ex = new InsufficientFundsException("Insufficient funds.");
            errorLogger.error("Account {} with balance ${} could not withdraw ${}",
                    accountID, account.getBalance(), String.format("%,.2f",amount / 100.0), ex);
            throw ex;
        }

        long newBalance = account.getBalanceExtendedCents() - amount;

        account.setBalanceExtendedCents(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction(
            0,
            Type.WITHDRAW,
            LocalDateTime.now(),
            amount,
            accountID,
            null
        );

        transactionRepository.save(transaction);

        actionLogger.info("Withdraw successfully made.");
        return transaction;
    }

    // Transfers money from one account to another
    public Transaction transfer(long sourceAccountID, long destinationAccountID, long amount) {
        actionLogger.info("Attempting to transfer ${} from Account {} to Account {}.",
                String.format("%,.2f",amount / 100.0), sourceAccountID, destinationAccountID);
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

        //Wrap both src and dst accounts into one list so accountRepository has to process both
        //at same time.
        List<Account> accounts = new ArrayList<>();
        accounts.add(source);
        accounts.add(destination);

        accountRepository.save(accounts);

        Transaction transaction = new Transaction(
            0,
            Type.TRANSFER,
            LocalDateTime.now(),
            amount,
            sourceAccountID,
            destinationAccountID
        );

        transactionRepository.save(transaction);

        actionLogger.info("Transfer successfully made.");
        return transaction;
    }

    /**
     * Returns the latest transactions given an accountID and the transaction history entry limit
     * @param accountID account id that is being queried
     * @param historyLimit number of transaction records that can be returned.
     * @return a list of the most recent transactions
     * @throws SQLException when there is a database connection failure
     * @throws NoTransactionHistoryException when there is no transaction history found for the given account
     */
    public List<Transaction> getTransactionHistory(long accountID, int historyLimit) throws SQLException, NoTransactionHistoryException {
        actionLogger.info("Attempting to get {} record(s) of transaction history from Account {}",
                historyLimit, accountID);
        try {
            List<Transaction> results = transactionRepository.getAudit(accountID, historyLimit);
            if(results == null) {
                NoTransactionHistoryException ex =
                        new NoTransactionHistoryException("No transaction history found for account: " + accountID);
                errorLogger.error("No transaction history found for Account {}.", accountID, ex);
                throw ex;
            }
            return results;
        } catch(SQLException ex){
            throw ex;
        }
    }

    // Ensures transaction amount is valid
    private void validateAmount(long amount) {
        actionLogger.info("Attempting to validate amount of ${}", String.format("%,.2f",amount / 100.0));
        if (amount <= 0) {
            InvalidAmountException ex =
                    new InvalidAmountException("Transaction amount must be greater than zero.");
            errorLogger.error("Invalid amount of {} provided", amount, ex);
            throw ex;
        }
        actionLogger.info("Amount successfully verified.");
    }

    private Account getAccountOrThrow(long accountID) {
        actionLogger.info("Attempting to locate Account {}", accountID);
        Account account = accountRepository.findByID(accountID);

        if (account == null) {
            AccountNotFoundException ex =
                    new AccountNotFoundException("Account " + accountID + " was not found.");
            errorLogger.error("Account {} could not be located", accountID);
            throw ex;
        }
        actionLogger.info("Account successfully located.");
        return account;
    }
}

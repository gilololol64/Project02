package com.bankofcli.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data model for a single transaction (deposit, withdraw, or transfer).
 * Gets written to the transactions table whenever an account's balance changes.
 *
 * accountSrc/accountDst follow the money: src is where funds leave from,
 * dst is where funds land. Depending on type, one side will be null:
 *   - DEPOSIT:  src = null,       dst = account being credited
 *   - WITHDRAW: src = account,    dst = null
 *   - TRANSFER: src = sender,     dst = receiver
 *
 * amount is stored in extended cents, same convention as accounts.balance
 * in the SQLite schema, to avoid floating point rounding errors.
 * $10.00 is stored as 1000.
 */
public class Transaction {

    public enum Type {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }

    private long transactionID;
    private Type type;
    private LocalDateTime timeComplete;
    private long amount;
    private Long accountSrc;
    private Long accountDst;

    /**
     * @param transactionID Unique ID for this transaction (primary key once persisted)
     * @param type DEPOSIT, WITHDRAW, or TRANSFER
     * @param timeComplete When the transaction happened
     * @param amount Amount moved in extended cents, always stored as positive
     * @param accountSrc Account funds are leaving, null for a deposit
     * @param accountDst Account funds are landing in, null for a withdraw
     */
    public Transaction(long transactionID, Type type, LocalDateTime timeComplete, long amount,
                        Long accountSrc, Long accountDst) {
        this.transactionID = transactionID;
        this.type = type;
        this.timeComplete = timeComplete;
        this.amount = amount;
        this.accountSrc = accountSrc;
        this.accountDst = accountDst;
    }

    public long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(long transactionID) {
        this.transactionID = transactionID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getTimeComplete() {
        return timeComplete;
    }

    public void setTimeComplete(LocalDateTime timeComplete) {
        this.timeComplete = timeComplete;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Long getAccountSrc() {
        return accountSrc;
    }

    public void setAccountSrc(Long accountSrc) {
        this.accountSrc = accountSrc;
    }

    public Long getAccountDst() {
        return accountDst;
    }

    public void setAccountDst(Long accountDst) {
        this.accountDst = accountDst;
    }

    /**
     * Method to compare if two Transaction Objects are the same.
     * Only checks if the transactionIDs of both objects match
     * @param o   the reference object with which to compare.
     * @return true if transaction IDs match, false if otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return transactionID == that.transactionID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(transactionID);
    }

    /**
     * @return a string representing the Transaction Object, displays its ID, type, and amount
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionID=" + transactionID +
                ", type=" + type +
                ", timeComplete=" + timeComplete +
                ", amount=$" + String.format("%.2f", amount / 100.0) +
                ", accountSrc=" + accountSrc +
                ", accountDst=" + accountDst +
                '}';
    }
}

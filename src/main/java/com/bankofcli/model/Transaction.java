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

    //Enum used to represent the three types of transactions the application accepts
    public enum Type {
        DEPOSIT,
        WITHDRAW,
        TRANSFER;

        public static Type getTypeFromString(String value){
            String lower = value.toLowerCase();
            switch(lower){
                case "deposit":
                    return DEPOSIT;
                case "withdraw":
                    return WITHDRAW;
                default:
                    return TRANSFER;
            }
        }
    }

    private int transactionID;
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
    public Transaction(int transactionID, Type type, LocalDateTime timeComplete, long amount,
                        Long accountSrc, Long accountDst) {
        this.transactionID = transactionID;
        this.type = type;
        this.timeComplete = timeComplete;
        this.amount = amount;
        this.accountSrc = accountSrc;
        this.accountDst = accountDst;
    }

    /* Returns transaction id
    * Note for newly created transactions in the program disgard this value as transaction ids
    * are generated in the database. When reading transactions from the database this value should be
    * appropriate and correct to use. */
    public int getTransactionID() {
        return transactionID;
    }

    /* Returns an enum representing the type of the transaction: Withdrawal, Deposit or Transfer */
    public Type getType() {
        return type;
    }

    /* Gets time this transaction was completed/processed */
    public LocalDateTime getTimeComplete() {
        return timeComplete;
    }

    /* Gets amount of the transaction */
    public long getAmount() {
        return amount;
    }

    /* Given the value in extended cents sets that as new transaction object's amount */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /* Returns Account Source for transaction, is null for a deposit */
    public Long getAccountSrc() {
        return accountSrc;
    }

    /* Returns Account Destination for transaction, is null for a withdrawal */
    public Long getAccountDst() {
        return accountDst;
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

        boolean accountSrcEqual = (this.accountSrc == null) ?
                that.accountSrc == null : this.accountSrc.equals(that.accountSrc);
        boolean accountDstEqual = (this.accountDst == null) ?
                that.accountDst == null : this.accountDst.equals(that.accountDst);

        return type == that.type && timeComplete.equals(that.timeComplete) &&
                amount == that.amount && accountSrcEqual && accountDstEqual;
    }

    /* Returns hash code for transaction object */
    @Override
    public int hashCode() {

        return Objects.hash(type, timeComplete, amount, accountSrc, accountDst);
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

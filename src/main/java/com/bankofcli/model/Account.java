package com.bankofcli.model;

import java.util.Objects;

/**
 * Class meant to represent the Data Model for an Account.
 * Holds an account's id, hashed pin and its current balance.
 */
public class Account {

    private long accountID;
    private int pinHash;
    private double balance;

    /**
     * Constructor for Account Data Class
     * @param accountID Unique Account ID used at login
     * @param pinHash Pin/Password for Account
     * @param balance Current balance of account
     */
    public Account(long accountID, int pinHash, double balance) {
        this.pinHash = pinHash;
        this.accountID = accountID;
        this.balance = balance;
    }

    public long getAccountID() {
        return accountID;
    }

    public void setAccountID(long accountID) {
        this.accountID = accountID;
    }

    public int getPinHash() {
        return pinHash;
    }

    //Ideally would have AccountServices generate a pin hash for account and then
    //temporary store it in Account Object to be updated in Database as well
    public void setPinHash(int pinHash) {
        this.pinHash = pinHash;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Method to compare if two Account Objects are the same.
     * Only checks if the accountIDs of both objects match
     * @param o   the reference object with which to compare.
     * @return true if account IDs match, false if otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountID == account.accountID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountID);
    }

    /**
     * @return a string representing the Account Object, displays its ID and current balance
     */
    @Override
    public String toString() {
        return "Account{" +
                "accountID=" + accountID +
                ", balance=" + balance +
                '}';
    }
}

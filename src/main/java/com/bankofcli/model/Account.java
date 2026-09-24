package com.bankofcli.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Class meant to represent the Data Model for an Account.
 * Holds an account's id, pin and its current balance.
 */
public class Account {

    private long accountID;
    private String pinHash;
    private long balanceExtendedCents;
    private Instant accountLockedTil = null;

    /**
     * Constructor for newly created Account Data Class
     * @param accountID Unique Account ID used at login
     * @param pinHash Hashed Pin/Password for Account
     * @param balanceExtendedCents Current balance of account representing as extended cents
     *                             (i.e. $10.00 = 1000)
     * accountLockedTil is set to null.
     */
    public Account(long accountID, String pinHash, long balanceExtendedCents) {
        this(accountID, pinHash, balanceExtendedCents, null);
    }

    /**
     * Constructor for retrieved Account Data Class
     * @param accountID Unique Account ID used at login
     * @param pinHash Hashed Pin/Password for Account
     * @param balanceExtendedCents Current balance of account representing as extended cents
     *                             (i.e. $10.00 = 1000)
     * @param accountLockedTil timestamp that represents when account can be unlocked.
     */
    public Account(long accountID, String pinHash, long balanceExtendedCents, Instant accountLockedTil) {
        this.pinHash = pinHash;
        this.accountID = accountID;
        this.balanceExtendedCents = balanceExtendedCents;
        this.accountLockedTil = accountLockedTil;
    }

    /* Return's account object's set account id */
    public long getAccountID() {
        return accountID;
    }

    /* Returns account's hashed pin */
    public String getPinHash() {
        return pinHash;
    }

    /* Sets account's hashed pin */
    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    /**
     * Returns account object's current balance in extended cents format (1000 = $10.00) */
    public long getBalanceExtendedCents() {
        return balanceExtendedCents;
    }

    /* Given the value in extended cents sets that as new account object's balance */
    public void setBalanceExtendedCents(long balanceExtendedCents) {
        this.balanceExtendedCents = balanceExtendedCents;
    }

    //Method used to get the balance as a double or dollar amount
    public double getBalance() { return this.balanceExtendedCents / 100.0; }

    //Gets the timestamp of when the account can be unlocked
    public Instant getAccountLockedTil() {
        return accountLockedTil;
    }

    public void setAccountLockedTil(Instant accountLockedTil){
        this.accountLockedTil = accountLockedTil;
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

    /* Returns hashcode for account object */
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
                ", balance=$" + String.format("%.2f", balanceExtendedCents / 100.0) +
                '}';
    }

}

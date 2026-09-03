package com.bankofcli.model;

import java.util.Objects;

/**
 * Class meant to represent the Data Model for an Account.
 * Holds an account's id, hashed pin and its current balance.
 */
public class Account {

    private long accountID;
    private int pin;
    private int balanceExtendedCents;

    /**
     * Constructor for Account Data Class
     * @param accountID Unique Account ID used at login
     * @param pin Pin/Password for Account
     * @param balanceExtendedCents Current balance of account representing as extended cents
     *                             (i.e. $10.00 = 1000)
     */
    public Account(long accountID, int pin, int balanceExtendedCents) {
        this.pin = pin;
        this.accountID = accountID;
        this.balanceExtendedCents = balanceExtendedCents;
    }

    public long getAccountID() {
        return accountID;
    }

    public void setAccountID(long accountID) {
        this.accountID = accountID;
    }

    public int getPin() {
        return pin;
    }

    //Ideally would have AccountServices generate a pin hash for account and then
    //temporary store it in Account Object to be updated in Database as well
    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getBalanceExtendedCents() {
        return balanceExtendedCents;
    }

    public void setBalanceExtendedCents(int balanceExtendedCents) {
        this.balanceExtendedCents = balanceExtendedCents;
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
                ", balance=" + balanceExtendedCents +
                '}';
    }
}

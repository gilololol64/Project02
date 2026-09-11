package com.bankofcli.model;

import java.util.Objects;

/**
 * Class meant to represent the Data Model for an Account.
 * Holds an account's id, hashed pin and its current balance.
 */
public class Account {

    private long accountID;
    private int pin;
    private long balanceExtendedCents;

    /**
     * Constructor for Account Data Class
     * @param accountID Unique Account ID used at login
     * @param pin Pin/Password for Account
     * @param balanceExtendedCents Current balance of account representing as extended cents
     *                             (i.e. $10.00 = 1000)
     */
    public Account(long accountID, int pin, long balanceExtendedCents) {
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

    public long getBalanceExtendedCents() {
        return balanceExtendedCents;
    }

    //
    public void setBalanceExtendedCents(long balanceExtendedCents) {
        this.balanceExtendedCents = balanceExtendedCents;
    }

    //Method used to get the balance as a double or dollar amount
    public double getBalance() { return this.balanceExtendedCents / 100; }

    //Class method to convert normal dollar amount to extended cents
    public static int balanceToExtendedCents(double balance) {
        return (int) balance * 100;
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
                ", balance=$" + String.format("%.2f", balanceExtendedCents / 100.0) +
                '}';
    }

}

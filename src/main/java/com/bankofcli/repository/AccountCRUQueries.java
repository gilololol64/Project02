package com.bankofcli.repository;

/**
 * Enum meant to hold the SQL Create, Read and Update Queries for the Accounts Table
 */
enum AccountCRUQueries {

    INSERT_UPDATE("INSERT INTO accounts(account_id,pin_hash,balance) " +
            "VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin_hash = EXCLUDED.pin_hash, balance = EXCLUDED.balance"),

    FIND_BY_ID("SELECT account_id,pin_hash,balance FROM accounts WHERE account_id = ?");

    private final String query;

    private AccountCRUQueries(String query){
        this.query = query;
    }

    String getQuery() {
        return this.query;
    }

}

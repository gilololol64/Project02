package com.bankofcli.database;

/**
 * Enum meant to hold the SQL Create Schema Queries
 */
enum TableCreationQueries {
    ACCOUNT("CREATE TABLE IF NOT EXISTS accounts ("
            + "    account_id BIGINT PRIMARY KEY,"
            + "    pin_hash TEXT NOT NULL,"
            + "    balance BIGINT NOT NULL"
            + ");"),

    TRANSACTION("CREATE TABLE IF NOT EXISTS transactions ("
                        + "	transaction_id INTEGER PRIMARY KEY,"
                        + " trans_type TEXT NOT NULL CHECK (trans_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),"
                        + "	time_complete TEXT NOT NULL DEFAULT (datetime('now')),"
                        + " amount BIGINT NOT NULL,"
                        + "	account_src BIGINT,"
                        + "	account_dst BIGINT,"
                        + "	FOREIGN KEY (account_src) REFERENCES accounts(account_id) ON DELETE CASCADE,"
                        + "	FOREIGN KEY (account_dst) REFERENCES accounts(account_id) ON DELETE CASCADE"
                        + ");");

    private final String query;

    private TableCreationQueries(String query){
        this.query = query;
    }

    String getQuery() {
        return this.query;
    }

}

package com.bankofcli.repository;

/**
 * Enum meant to hold the SQL Create and Read and Update Queries for the Transaction Table
 */
enum TransactionCRQueries{

    INSERT("INSERT INTO transactions (trans_type, time_complete, amount, account_src, account_dst)"
            + "   VALUES (?,?,?,?,?)"),
    READ("SELECT *\n" +
            "FROM (SELECT * FROM transactions\n" +
                    "WHERE account_dst = ? \n" +
                    "UNION\n" +
                    "SELECT * FROM transactions\n" +
                    "WHERE account_src = ?)\n" +
                    "ORDER BY time_complete DESC LIMIT ?;");

    private final String query;

    TransactionCRQueries(String query){
        this.query = query;
    }

    String getQuery() {
        return this.query;
    }
}

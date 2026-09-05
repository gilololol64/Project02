package com.bankofcli.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
	
	private static final String url="jdbc:sqlite:BigBankersBank.db";
	
	public static void main(String[] args) {
		new DatabaseManager().init();
	}
	
	public Connection open() {
		//returns a open connection to the SQLite Database
		try {
		return DriverManager.getConnection(url);
		}catch(SQLException e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
	}
	public void close(Connection current) {
		//closes the connection to the database
		try {
			current.close();
		} catch (SQLException e) {
			 throw new RuntimeException("Could not close connection", e);
		}
		
	}
	
	public void init() {
		var sqlCreateAccount ="CREATE TABLE IF NOT EXISTS accounts ("
						+ "    account_id BIGINT PRIMARY KEY,"
						+ "    pin INT NOT NULL,"
						+ "    balance INT NOT NULL"
						+ ");";
		var sqlCreateTransactions ="CREATE TABLE IF NOT EXISTS transactions ("
							+ "	transaction_id BIGINT PRIMARY KEY,"
							+ " trans_type TEXT NOT NULL CHECK (trans_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),"
							+ "	time_complete TEXT NOT NULL DEFAULT (datetime('now')),"
							+ " amount INT NOT NULL,"
							+ "	account_src BIGINT NOT NULL,"
							+ "	account_dst BIGINT,"
							+ "	FOREIGN KEY (account_src) REFERENCES account(account_id),"
							+ "	FOREIGN KEY (account_dst) REFERENCES account(account_id)"
							+ ");";
		
		 try (var conn = DriverManager.getConnection(url)){
			 var stmtA = conn.createStatement();
			 //create two tables
			 stmtA.execute(sqlCreateAccount);
			 stmtA.execute(sqlCreateTransactions);
		 }catch(SQLException e){
			 e.printStackTrace();
		 }
	}
	
}

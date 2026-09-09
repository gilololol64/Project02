package com.bankofcli.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.bankofcli.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

	private static final String url="jdbc:sqlite:BigBankersBank.db";
	

	public Connection open() {
		//returns a open connection to the SQLite Database
		try {
		return DriverManager.getConnection(url);
		}catch(SQLException e) {
			logger.error("Could not open a connection to the database.", e);
	        throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}
	public void close(Connection current) {
		//closes the connection to the database
		try {
			current.close();
		} catch (SQLException e) {
			logger.error("Could not close the database connection.", e);
			 throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		} catch(NullPointerException e){
			throw new NullPointerException("Could not close empty database connection.");
		}
		
	}
	
	public void init() {
		var sqlCreateAccount ="CREATE TABLE IF NOT EXISTS accounts ("
						+ "    account_id BIGINT PRIMARY KEY,"
						+ "    pin INT NOT NULL,"
						+ "    balance BIGINT NOT NULL"
						+ ");";
		var sqlCreateTransactions ="CREATE TABLE IF NOT EXISTS transactions ("
							+ "	transaction_id BIGINT PRIMARY KEY,"
							+ " trans_type TEXT NOT NULL CHECK (trans_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')),"
							+ "	time_complete TEXT NOT NULL DEFAULT (datetime('now')),"
							+ " amount BIGINT NOT NULL,"
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
			 logger.info("Database tables verified/created successfully.");
		 }catch(SQLException e){
			 logger.error("Could not initialize database tables.", e);
		 }
	}
	
}

package com.bankofcli.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.bankofcli.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

	// Dedicated error logger - name must match logback.xml's <logger> element
	// exactly to route to the SQL error log file instead of falling through to root.
	private static final Logger sqlErrorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");

	//General error logger
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");

	// General action logger - name doesn't matter, inherits from root and
	// lands in the AccountAction log file. Only ever used for .info() calls.
	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

	private static final String url="jdbc:sqlite:StoneVaultBank.db?foreign_keys=true";

	/* Returns a Connection Object to the location of the Bank's database. */
	public Connection open() {
		actionLogger.info("Attempting to connect to database.");
		//returns an open connection to the SQLite Database
		try {
			Connection con = DriverManager.getConnection(url);
			actionLogger.info("Connection successfully established.");
		return con;
		}catch(SQLException e) {
			sqlErrorLogger.error("Could not open a connection to the database.", e);
	        throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}

	/* Closes a given Connection Object to the Program's database */
	public void close(Connection current) {
		//closes the connection to the database
		actionLogger.info("Attempting to close database connection.");
		try {
			current.close();
			actionLogger.info("Database connection successfully closed");
		} catch (SQLException e) {
			sqlErrorLogger.error("Could not close the database connection.", e);
			 throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		} catch(NullPointerException e){
			errorLogger.error("Null database connection passed into function", e);
			throw new NullPointerException("Could not close empty database connection.");
		}
		
	}

	/* Initializes and creates database or tables if none previously exist */
	public void init() {
		actionLogger.info("Attempting to verify or initialize database.");

		var sqlCreateAccount = TableCreationQueries.ACCOUNT.getQuery();
		var sqlCreateTransactions = TableCreationQueries.TRANSACTION.getQuery();
		
		 try (var conn = DriverManager.getConnection(url)){
			 var stmtA = conn.createStatement();
			 //create two tables
			 stmtA.execute(sqlCreateAccount);
			 stmtA.execute(sqlCreateTransactions);
			 actionLogger.info("Database tables verified/created successfully.");
		 }catch(SQLException e){
			 sqlErrorLogger.error("Could not initialize database tables.", e);
		 }
	}
	
}

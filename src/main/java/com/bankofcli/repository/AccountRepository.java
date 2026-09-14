package com.bankofcli.repository;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.exception.ServiceUnavailableException;
import com.bankofcli.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountRepository {

	// Dedicated error logger - name must match logback.xml's <logger> element
	// exactly to route to the SQL error log file instead of falling through to root.
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");

	// General action logger - name doesn't matter, inherits from root and
	// lands in the AccountAction log file. Only ever used for .info() calls.
	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

	DatabaseManager db;
	//creates a log
	public AccountRepository() {
		db = new DatabaseManager();
		db.init(); //Added init statement in case database does not exist yet
		
	}
	// Test Account with id 123456 in DB
	public Account findByID(long accountID) {
		actionLogger.info("Searching database for account with id: {}.", accountID);

		var sql ="SELECT account_id,pin,balance FROM accounts WHERE account_id = ?";

		try(var conn = db.open();
			var stmt = conn.prepareStatement(sql)){
			//Executes select statement and returns Account variable

			stmt.setLong(1, accountID);
			ResultSet rs = stmt.executeQuery();
			//Check if there were any results from the query before creating empty Account object
			if (rs.next()) {
				Account result = new Account(rs.getLong("account_id"), rs.getInt("pin"), rs.getLong("balance"));
				rs.close();
				actionLogger.info("Result found for account id {}", accountID);
				return result;
			}
			rs.close();
			actionLogger.info("No results found for account id {}", accountID);
			return null;
		}catch(SQLException e) {
			errorLogger.error("Database error while looking up account {}.", accountID, e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
		
	}
	
	public void save(List<Account> toBeSaved) {
		if(toBeSaved == null || toBeSaved.isEmpty()){
			NullPointerException ex = new NullPointerException("Can not insert empty list of accounts");
			errorLogger.error("Account list provided was empty or null.", ex);
			throw ex;
		}

		actionLogger.info("Attempting to save {} account(s) to database", toBeSaved.size());

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		try(var conn =db.open()){
			try(var stmt = conn.prepareStatement(sql);) {
				conn.setAutoCommit(false);
				for (Account account : toBeSaved) {
					stmt.setLong(1, account.getAccountID());
					stmt.setInt(2, account.getPin());
					stmt.setLong(3, account.getBalanceExtendedCents());
					stmt.executeUpdate();
				}
				conn.commit();
				actionLogger.info("Successfully saved {} account(s).", toBeSaved.size());
	
			} catch (SQLException e) {
				conn.rollback();
				conn.close();
				errorLogger.error("Database error while saving a batch of {} account(s).", toBeSaved.size(), e);
				throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
			}
		} catch (SQLException e1) {
			errorLogger.error("Database error while saving a batch of {} account(s).", toBeSaved.size(), e1);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e1);
		}
		
	}
	
	public void save(Account toBeSaved) {
		if(toBeSaved == null){
			NullPointerException ex = new NullPointerException("Can not insert an empty account");
			errorLogger.error("Account provided was null.", ex);
			throw ex;
		}

		actionLogger.info("Attempting to save account: {} to database", toBeSaved.getAccountID());

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		
		try(var conn =db.open();
			var stmt = conn.prepareStatement(sql)
			) {
			stmt.setLong(1, toBeSaved.getAccountID());
			stmt.setInt(2, toBeSaved.getPin());
			stmt.setLong(3, toBeSaved.getBalanceExtendedCents());
			stmt.executeUpdate();
			actionLogger.info("Successfully saved account {}.", toBeSaved.getAccountID());
		} catch (SQLException e) {
			errorLogger.error("Database error while saving account {}.", toBeSaved.getAccountID(), e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}
}

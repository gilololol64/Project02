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
	private static final Logger sqlErrorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");

	// General action logger - name doesn't matter, inherits from root and
	// lands in the AccountAction log file. Only ever used for .info() calls.
	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

	//General error logger
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");

	DatabaseManager db;
	//creates a log
	public AccountRepository() {
		db = new DatabaseManager();
		db.init(); //Added init statement in case database does not exist yet
		
	}
	// Given an account id, retrieves that account if it exists, if not returns null
	public Account findByID(long accountID) {
		actionLogger.info("Searching database for account with id: {}.", accountID);

		var sql ="SELECT account_id,pin_hash,balance FROM accounts WHERE account_id = ?";

		try(var conn = db.open();
			var stmt = conn.prepareStatement(sql)){
			//Executes select statement and returns Account variable

			stmt.setLong(1, accountID);
			ResultSet rs = stmt.executeQuery();
			//Check if there were any results from the query before creating empty Account object
			if (rs.next()) {
				Account result = new Account(rs.getLong("account_id"), rs.getString("pin_hash"), rs.getLong("balance"));
				rs.close();
				actionLogger.info("Result found for account id {}", accountID);
				return result;
			}
			rs.close();
			actionLogger.info("No results found for account id {}", accountID);
			return null;
		}catch(SQLException e) {
			sqlErrorLogger.error("Database error while looking up account {}.", accountID, e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
		
	}

	/**
	 * Given a list of Account it will insert/update all those accounts to the database.
	 * Should be used to update accounts when preforming a transfer as auto commiting is set to false.
	 * @param toBeSaved list of account objects to be saved to the database
	 */
	public void save(List<Account> toBeSaved) {
		if(toBeSaved == null || toBeSaved.isEmpty()){
			NullPointerException ex = new NullPointerException("Can not insert empty list of accounts");
			sqlErrorLogger.error("Account list provided was empty or null.", ex);
			throw ex;
		}

		actionLogger.info("Attempting to save {} account(s) to database", toBeSaved.size());

		var sql ="INSERT INTO accounts(account_id,pin_hash,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin_hash = EXCLUDED.pin_hash, balance = EXCLUDED.balance";
		try(var conn =db.open()){
			try(var stmt = conn.prepareStatement(sql);) {
				conn.setAutoCommit(false);
				for (Account account : toBeSaved) {
					stmt.setLong(1, account.getAccountID());
					stmt.setString(2, account.getPinHash());
					stmt.setLong(3, account.getBalanceExtendedCents());
					stmt.executeUpdate();
				}
				conn.commit();
				actionLogger.info("Successfully saved {} account(s).", toBeSaved.size());
	
			} catch (SQLException e) {
				conn.rollback();
				conn.close();
				sqlErrorLogger.error("Database error while saving a batch of {} account(s).", toBeSaved.size(), e);
				throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
			}
		} catch (SQLException e1) {
			sqlErrorLogger.error("Database error while saving a batch of {} account(s).", toBeSaved.size(), e1);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e1);
		}
		
	}

	/**
	 * Given an account inserts/updates that account object to the database
	 * @param toBeSaved account that is going to be saved
	 */
	public void save(Account toBeSaved) {
		if(toBeSaved == null){
			NullPointerException ex = new NullPointerException("Can not insert an empty account");
			errorLogger.error("Account provided was null.", ex);
			throw ex;
		}

		actionLogger.info("Attempting to save account: {} to database", toBeSaved.getAccountID());

		var sql ="INSERT INTO accounts(account_id,pin_hash,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin_hash = EXCLUDED.pin_hash, balance = EXCLUDED.balance";
		
		try(var conn =db.open();
			var stmt = conn.prepareStatement(sql)
			) {
			stmt.setLong(1, toBeSaved.getAccountID());
			stmt.setString(2, toBeSaved.getPinHash());
			stmt.setLong(3, toBeSaved.getBalanceExtendedCents());
			stmt.executeUpdate();
			actionLogger.info("Successfully saved account {}.", toBeSaved.getAccountID());
		} catch (SQLException e) {
			sqlErrorLogger.error("Database error while saving account {}.", toBeSaved.getAccountID(), e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}
}

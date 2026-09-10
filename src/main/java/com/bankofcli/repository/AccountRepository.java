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

	private static final Logger logger = LoggerFactory.getLogger("SQLErrors");

	DatabaseManager db;
	//creates a log
	public AccountRepository() {
		db = new DatabaseManager();
		db.init(); //Added init statement in case database does not exist yet
		
	}
	// Test Account with id 123456 in DB
	public Account findByID(long accountID) {
		
		var sql ="SELECT account_id,pin,balance FROM accounts WHERE account_id = ?";

		try(var conn = db.open();
			var stmt = conn.prepareStatement(sql)){
			//Executes select statement and returns Account variable

			stmt.setLong(1, accountID);
			ResultSet rs = stmt.executeQuery();
			//Check if there were any results from the query before creating empty Account object
			if (rs.next()) {
				Account result = new Account(rs.getLong("account_id"), rs.getInt("pin"), rs.getInt("balance"));
				rs.close();
				return result;
			}
			rs.close();
			return null;
		}catch(SQLException e) {
			logger.error("Database error while looking up account {}.", accountID, e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
		
	}
	
	public void save(List<Account> toBeSaved) {

		if(toBeSaved == null || toBeSaved.isEmpty()){
			throw new NullPointerException("Can not insert empty list of accounts");
		}

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		
		try(var conn =db.open();
			var stmt = conn.prepareStatement(sql)) {
			conn.setAutoCommit(false);
			for (Account account : toBeSaved) {
				stmt.setLong(1, account.getAccountID());
				stmt.setInt(2, account.getPin());
				stmt.setLong(3, account.getBalanceExtendedCents());
				stmt.executeUpdate();
			}
			conn.commit();
			logger.info("Successfully saved {} account(s).", toBeSaved.size());

		} catch (SQLException e) {
			logger.error("Database error while saving a batch of {} account(s).", toBeSaved.size(), e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
		
	}
	
	public void save(Account toBeSaved) {

		if(toBeSaved == null){
			throw new NullPointerException("Can not insert an empty account");
		}

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		
		try(var conn =db.open();
			var stmt = conn.prepareStatement(sql);
			) {
			stmt.setLong(1, toBeSaved.getAccountID());
			stmt.setInt(2, toBeSaved.getPin());
			stmt.setLong(3, toBeSaved.getBalanceExtendedCents());
			stmt.executeUpdate();
			logger.info("Successfully saved account {}.", toBeSaved.getAccountID());
		} catch (SQLException e) {
			logger.error("Database error while saving account {}.", toBeSaved.getAccountID(), e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}


	public void update(Account account) {
	}
}

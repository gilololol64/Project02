package com.bankofcli.repository;

import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.model.Account;

public class AccountRepository {
	
	DatabaseManager db;
	//creates a log
	public AccountRepository() {
		db = new DatabaseManager();
		db.init(); //Added init statement in case database does not exist yet
		
	}
	// Test Account with id 123456 in DB
	public Account findByID(Long accountID) {
		
		var sql ="SELECT account_id,pin,balance FROM accounts WHERE account_id = ?";
		try(var conn = db.open()){
			//Executes select statement and returns Account variable
			var stmt= conn.prepareStatement(sql);
			stmt.setString(1, accountID.toString());
			ResultSet rs =stmt.executeQuery();

			//Check if there were any results from the query before creating empty Account object
			if (rs.next()) {
				return new Account(rs.getLong("account_id"), rs.getInt("pin"), rs.getInt("balance"));
			}
			return null;
		}catch(SQLException e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
		
	}
	
	public void save(List<Account> toBeSaved) {

		if(toBeSaved == null || toBeSaved.isEmpty()){
			throw new NullPointerException("Can not insert empty list of accounts");
		}

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		
		try(var conn =db.open()) {
			conn.setAutoCommit(false);
			for (Account account : toBeSaved) {
				var stmt = conn.prepareStatement(sql);
				stmt.setLong(1, account.getAccountID());
				stmt.setInt(2, account.getPin());
				stmt.setDouble(3, account.getBalanceExtendedCents());
				stmt.executeUpdate();
			}
			conn.commit();
			
		} catch (SQLException e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
		
	}
	
	public void save(Account toBeSaved) {

		if(toBeSaved == null){
			throw new NullPointerException("Can not insert an empty account");
		}

		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?) ON CONFLICT (account_id) DO UPDATE SET pin = EXCLUDED.pin, balance = EXCLUDED.balance";
		
		try(var conn =db.open()) {
			var stmt = conn.prepareStatement(sql);
			stmt.setLong(1, toBeSaved.getAccountID());
			stmt.setInt(2, toBeSaved.getPin());
			stmt.setDouble(3, toBeSaved.getBalanceExtendedCents());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
	}
	
	

}

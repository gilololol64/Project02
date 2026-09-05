package com.bankofcli.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.model.Account;

public class AccountRepository {
	
	DatabaseManager db;
	//creates a log
	public AccountRepository() {
		db = new DatabaseManager();
	
	}	
	
	public Account findByID(Long accountID) {
		
		var sql ="SELECT account_id,pin,balance FROM accounts WHERE account_id = ?";
		try(var conn = db.open()){
			//Executes select statement and returns Account variable
			var stmt= conn.prepareStatement(sql);
			stmt.setString(1, accountID.toString());
			ResultSet rs =stmt.executeQuery();
			Account found = new Account(rs.getLong("account_id"), rs.getInt("pin"), rs.getInt("balance"));
			return found;
		}catch(SQLException e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
		
	}
	public void save(Account toBeSaved) {
		var sql ="UPDATE account SET pin = ?, balance = ? WHERE = ?";
		try(var conn =db.open()) {
			var stmt = conn.prepareStatement(sql);
			stmt.setInt(1, toBeSaved.getPin());
			stmt.setDouble(2, toBeSaved.getBalanceExtendedCents());
			stmt.setLong(3, toBeSaved.getAccountID());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.print("failure: " + e.getMessage());
	        throw new RuntimeException("Could not connect to database", e);
		}
		
		
		
	}
	
	public void saveNew(Account toBeSaved) {
		var sql ="INSERT INTO accounts(account_id,pin,balance) VALUES(?,?,?)";
		
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

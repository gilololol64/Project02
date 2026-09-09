package com.bankofcli.repository;


import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.model.Transaction;



public class TransactionRepository {
	DatabaseManager db;
	Logger ErrorLogger;
	
	public TransactionRepository() {
		DatabaseManager db = new DatabaseManager();
		ErrorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");
	}
	
	
	public void save(Transaction tobeSaved) {
		var sql ="   INSERT INTO transactions (transaction_id, trans_type, time_complete, amount, account_src, account_dst)"
				+ "   VALUES (?,?,?,?,?,?)"
				+ "   ON CONFLICT (transaction_id)"
				+ "   DO UPDATE SET trans_type=EXCLUDED.trans_type, time_complete=EXCLUDED.time_complete,"
				+ "   amount=EXCLUDED.amount, account_src=EXCLUDED.account_src, account_dst=EXCLUDED.account_dst";
		try (var conn =db.open();
			var stmt =conn.prepareStatement(sql);	
		){
			stmt.setLong(1,tobeSaved.getTransactionID());
			stmt.setString(2, tobeSaved.getType().name());
			stmt.setString(3,tobeSaved.getTimeComplete().toString());
			stmt.setLong(4, tobeSaved.getAmount());
			stmt.setLong(5, tobeSaved.getAccountSrc());
			stmt.setLong(6, tobeSaved.getAccountDst());
			
		} catch (SQLException e) {
			ErrorLogger.error("Database can not be added", e);
		}
	}
	
	
	public static void logTransaction(Transaction tobeLogged) {
		int firstDigit = Integer.parseInt(Long.toString(tobeLogged.getAccountSrc()).substring(0, 1));
		
		Logger curLogger = LoggerFactory.getLogger("Bank.Transaction.logback."+firstDigit);
		curLogger.info(tobeLogged.toString());
		
	}

    public void save(Transaction transaction) {

    }
}

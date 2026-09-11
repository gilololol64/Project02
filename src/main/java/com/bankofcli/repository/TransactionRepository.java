package com.bankofcli.repository;


import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.exception.ServiceUnavailableException;
import com.bankofcli.model.Transaction;



public class TransactionRepository {
	DatabaseManager db;
	Logger ErrorLogger;
	
	public TransactionRepository() {
		db = new DatabaseManager();
		ErrorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");
	}
	
	
	public void save(Transaction tobeSaved) {
		var sql ="   INSERT INTO transactions (transaction_id, trans_type, time_complete, amount, account_src, account_dst)"
				+ "   VALUES (?,?,?,?,?,?)"
				+ "   ON CONFLICT (transaction_id)"
				+ "   DO UPDATE SET trans_type=EXCLUDED.trans_type, time_complete=EXCLUDED.time_complete,"
				+ "   amount=EXCLUDED.amount, account_src=EXCLUDED.account_src, account_dst=EXCLUDED.account_dst";
		try (var conn =db.open();
			var stmt =conn.prepareStatement(sql)
		){
			stmt.setLong(1, nextTransactionID(conn));
			stmt.setString(2, tobeSaved.getType().name());
			stmt.setString(3,tobeSaved.getTimeComplete().toString());
			stmt.setLong(4, tobeSaved.getAmount());
			//Check for case that account src is null: Deposits
			if(tobeSaved.getAccountSrc() == null) {
				stmt.setNull(5, Types.BIGINT);
			} else {
				stmt.setLong(5, tobeSaved.getAccountSrc());
			}
			//Check for case that account dst is null: Withdraws
			if(tobeSaved.getAccountDst() == null) {
				stmt.setNull(6, Types.BIGINT);
			} else {
				stmt.setLong(6, tobeSaved.getAccountDst());
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			ErrorLogger.error("Database transaction could not be saved.", e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}

	private long nextTransactionID(java.sql.Connection connection) throws SQLException {
		try (var statement = connection.createStatement();
				var results = statement.executeQuery("SELECT COALESCE(MAX(transaction_id), 0) + 1 FROM transactions")) {
			return results.next() ? results.getLong(1) : 1L;
		}
	}
	
	
	public static void logTransaction(Transaction tobeLogged) {
		int firstDigit = Integer.parseInt(Long.toString(tobeLogged.getAccountSrc()).substring(0, 1));
		
		Logger curLogger = LoggerFactory.getLogger("Bank.Transaction.logback."+firstDigit);
		curLogger.info(tobeLogged.toString());
		
	}

}

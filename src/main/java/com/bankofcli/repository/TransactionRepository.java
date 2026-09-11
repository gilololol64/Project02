package com.bankofcli.repository;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bankofcli.database.DatabaseManager;
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
			stmt.setLong(1,tobeSaved.getTransactionID());
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
			
		} catch (SQLException e) {
			ErrorLogger.error("Database can not be added", e);
		}
	}

	public List<Transaction> getAudit(long accountID) throws SQLException {

        ArrayList<Transaction> transactions = new ArrayList<>();

		String sql = "SELECT *\n" +
				"FROM (SELECT * FROM transactions\n" +
				"WHERE account_dst = ? \n" +
				"UNION\n" +
				"SELECT * FROM transactions\n" +
				"WHERE account_src = ?)\n" +
				"ORDER BY date(time_complete) DESC;";

		try(var con = db.open();
			var ps = con.prepareStatement(sql)) {
			ps.setLong(1, accountID);
			ps.setLong(2, accountID);

			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()){
					long transID = rs.getLong("transaction_id");
					String transType = rs.getString("trans_type");
					Transaction.Type type = Transaction.Type.getTypeFromString(transType);
					String rawDate = rs.getString("time_complete");
					LocalDateTime timeComplete = LocalDateTime.parse(rawDate,
							DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					long amount = rs.getLong("amount");
					long accountSrc = (type != Transaction.Type.DEPOSIT) ? rs.getLong("account_src") : null;
					long accountDst = (type != Transaction.Type.WITHDRAW) ? rs.getLong("account_dst") : null;
					transactions.add(new Transaction(transID, type, timeComplete, amount, accountSrc, accountDst));
				}
			}
		} catch(SQLException ex){
			throw new SQLException("Could not retrieve transaction history from account id: " + accountID);
		}
		return transactions;
	}
	
	
	public static void logTransaction(Transaction tobeLogged) {
		int firstDigit = Integer.parseInt(Long.toString(tobeLogged.getAccountSrc()).substring(0, 1));
		
		Logger curLogger = LoggerFactory.getLogger("Bank.Transaction.logback."+firstDigit);
		curLogger.info(tobeLogged.toString());
		
	}

}

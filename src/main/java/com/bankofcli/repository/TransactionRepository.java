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
import com.bankofcli.exception.ServiceUnavailableException;
import com.bankofcli.model.Transaction;



public class TransactionRepository {
	DatabaseManager db;
	Logger ErrorLogger;

	// General action logger - name doesn't matter, inherits from root and
	// lands in the AccountAction log file. Only ever used for .info() calls.
	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");

	// Dedicated error logger - name must match logback.xml's <logger> element
	// exactly to route to the SQL error log file instead of falling through to root.
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");
	
	public TransactionRepository() {
		db = new DatabaseManager();
		ErrorLogger = LoggerFactory.getLogger("Bank.logback.SQLError");
	}
	
	
	public void save(Transaction tobeSaved) {

		if(tobeSaved == null){
			NullPointerException ex = new NullPointerException("Can not save empty transaction.");
			errorLogger.error("Transaction provided was null.", ex);
			throw ex;
		}

		actionLogger.info("Attempting to add transaction to database.");

		var sql ="   INSERT INTO transactions (trans_type, time_complete, amount, account_src, account_dst)"
				+ "   VALUES (?,?,?,?,?)";
		try (var conn =db.open();
			var stmt =conn.prepareStatement(sql)
		){
			stmt.setString(1, tobeSaved.getType().name());
			stmt.setString(2,tobeSaved.getTimeComplete().toString());
			stmt.setLong(3, tobeSaved.getAmount());
			//Check for case that account src is null: Deposits
			if(tobeSaved.getAccountSrc() == null) {
				stmt.setNull(4, Types.BIGINT);
			} else {
				stmt.setLong(4, tobeSaved.getAccountSrc());
			}
			//Check for case that account dst is null: Withdraws
			if(tobeSaved.getAccountDst() == null) {
				stmt.setNull(5, Types.BIGINT);
			} else {
				stmt.setLong(5, tobeSaved.getAccountDst());
			}
			stmt.executeUpdate();
			actionLogger.info("Transaction successfully added to database.");
		} catch (SQLException e) {
			ErrorLogger.error("Database transaction could not be saved.", e);
			throw new ServiceUnavailableException("Service temporarily unavailable, please try again later.", e);
		}
	}

	public List<Transaction> getAudit(long accountID, int historyLimit) throws SQLException {

		if(historyLimit < 1){
			IllegalArgumentException ex = new IllegalArgumentException("The max transaction history displayed amount must be greater than 1.");
			errorLogger.error("Invalid value for historyLimit passed in: {}.", historyLimit, ex);
			throw ex;
		}

		actionLogger.info("Attempting to retrieve {} record(s) of Account:{} most recent transactions",
				historyLimit, accountID);

        ArrayList<Transaction> transactions = new ArrayList<>();

		String sql = "SELECT *\n" +
				"FROM (SELECT * FROM transactions\n" +
				"WHERE account_dst = ? \n" +
				"UNION\n" +
				"SELECT * FROM transactions\n" +
				"WHERE account_src = ?)\n" +
				"ORDER BY time_complete DESC LIMIT ?;";

		try(var con = db.open();
			var ps = con.prepareStatement(sql)) {
			ps.setLong(1, accountID);
			ps.setLong(2, accountID);
			ps.setInt(3, historyLimit);

			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()){
					int transID = rs.getInt("transaction_id");
					String transType = rs.getString("trans_type");
					Transaction.Type type = Transaction.Type.getTypeFromString(transType);
					String rawDate = rs.getString("time_complete");
					LocalDateTime timeComplete = LocalDateTime.parse(rawDate);
					long amount = rs.getLong("amount");
					Long accountSrc = (type != Transaction.Type.DEPOSIT) ? rs.getLong("account_src") : null;
					Long accountDst = (type != Transaction.Type.WITHDRAW) ? rs.getLong("account_dst") : null;
					transactions.add(new Transaction(transID, type, timeComplete, amount, accountSrc, accountDst));
				}
			}
		} catch(SQLException ex){
			ex = new SQLException("Could not retrieve transaction history from account id: " + accountID);
			errorLogger.error("Could not retrieve transaction history due to database error", ex);
			throw ex;
		}
		actionLogger.info("Transaction history successfully retrieved.");
		return (transactions.isEmpty()) ? null : transactions;
	}

}

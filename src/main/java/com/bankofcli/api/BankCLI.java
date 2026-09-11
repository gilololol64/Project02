package com.bankofcli.api;

import com.bankofcli.database.DatabaseManager;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.TransactionService;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankCLI {

	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
	private static final String STARTUP_SCREEN = "/text_graphics/startUpScreenText.txt";
	private static final int HISTORY_LIMIT = 10;

	private final Scanner scanner;
	private final PrintStream output;
	private final AccountService accountService;
	private final TransactionService transactionService;
	private final DatabaseManager databaseManager;
	private boolean running;
	private Long loggedInAccountId;

	public BankCLI() {
		this(new Scanner(System.in), System.out, new AccountRepository(), new TransactionRepository(), new DatabaseManager());
	}

	public BankCLI(Scanner scanner, PrintStream output) {
		this(scanner, output, new AccountRepository(), new TransactionRepository(), new DatabaseManager());
	}

	BankCLI(Scanner scanner, PrintStream output, AccountRepository accountRepository,
			TransactionRepository transactionRepository, DatabaseManager databaseManager) {
		this.scanner = scanner;
		this.output = output;
		this.accountService = new AccountService(accountRepository);
		this.transactionService = new TransactionService(accountRepository, transactionRepository);
		this.databaseManager = databaseManager;
	}

	/** Starts the terminal application. Business rules belong in the service layer. */
	public void run() {
		running = true;
		printStartupScreen();
		output.println("===============Welcome to Bank of CLI===============");

		while (running) {
			if (loggedInAccountId == null) {
				showGuestMenu();
			} else {
				showAccountMenu();
			}
		}

		output.println("Thank you for using Bank of CLI.");
	}

	private void showGuestMenu() {
		output.println("\n1. Register");
		output.println("2. Log in");
		output.println("0. Exit");

		switch (readMenuChoice()) {
			case 1 -> register();
			case 2 -> logIn();
			case 0 -> running = false;
			default -> output.println("Please choose one of the listed options.");
		}
	}

	private void showAccountMenu() {
		output.println("\nAccount: " + loggedInAccountId);
		output.println("1. Check balance");
		output.println("2. Deposit");
		output.println("3. Withdraw");
		output.println("4. Transfer");
		output.println("5. Transaction history");
		output.println("6. Log out");
		output.println("0. Exit");

		switch (readMenuChoice()) {
			case 1 -> checkBalance();
			case 2 -> deposit();
			case 3 -> withdraw();
			case 4 -> transfer();
			case 5 -> showTransactionHistory();
			case 6 -> logOut();
			case 0 -> running = false;
			default -> output.println("Please choose one of the listed options.");
		}
	}

	private int readMenuChoice() {
		output.print("Choose an option: ");
		if (!scanner.hasNextLine()) {
			running = false;
			return 0;
		}

		try {
			return Integer.parseInt(scanner.nextLine().trim());
		} catch (NumberFormatException exception) {
			return -1;
		}
	}

	private void register() {
		Integer pin = readInteger("Enter a four-digit PIN: ");
		if (pin == null) return;

		try {
			Account account = accountService.register(pin);
			actionLogger.info("User successfully registered account {}.", account.getAccountID());
			output.println("Registration successful. Your Account ID is: " + account.getAccountID());
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void logIn() {
		Long accountId = readLong("Account ID: ");
		Integer pin = readInteger("PIN: ");
		if (accountId == null || pin == null) return;

		try {
			accountService.login(accountId, pin);
			loggedInAccountId = accountId;
			actionLogger.info("User successfully logged in to account {}.", accountId);
			output.println("Login successful.");
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void checkBalance() {
		try {
			output.printf("Current balance: $%,.2f%n", toDollars(accountService.getBalance(loggedInAccountId)));
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void deposit() {
		Long amount = readAmount();
		if (amount == null) return;

		try {
			transactionService.deposit(loggedInAccountId, amount);
			actionLogger.info("Deposit completed for account {}: {} cents.", loggedInAccountId, amount);
			output.printf("Deposit successful. New balance: $%,.2f%n",
					toDollars(accountService.getBalance(loggedInAccountId)));
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void withdraw() {
		Long amount = readAmount();
		if (amount == null) return;

		try {
			transactionService.withdraw(loggedInAccountId, amount);
			actionLogger.info("Withdrawal completed for account {}: {} cents.", loggedInAccountId, amount);
			output.printf("Withdrawal successful. New balance: $%,.2f%n",
					toDollars(accountService.getBalance(loggedInAccountId)));
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void transfer() {
		Long destinationId = readLong("Destination Account ID: ");
		Long amount = readAmount();
		if (destinationId == null || amount == null) return;

		try {
			transactionService.transfer(loggedInAccountId, destinationId, amount);
			actionLogger.info("Transfer completed from account {} to account {}: {} cents.",
					loggedInAccountId, destinationId, amount);
			output.println("Transfer successful.");
		} catch (BankException exception) {
			showError(exception);
		}
	}

	private void showTransactionHistory() {
		String sql = "SELECT transaction_id, trans_type, time_complete, amount, account_src, account_dst "
				+ "FROM transactions WHERE account_src = ? OR account_dst = ? "
				+ "ORDER BY time_complete DESC LIMIT ?";

		try (var connection = databaseManager.open(); var statement = connection.prepareStatement(sql)) {
			statement.setLong(1, loggedInAccountId);
			statement.setLong(2, loggedInAccountId);
			statement.setInt(3, HISTORY_LIMIT);
			try (var results = statement.executeQuery()) {
				boolean found = false;
				while (results.next()) {
					found = true;
					output.printf("%s | %s | $%,.2f | from %s to %s | %s%n",
							results.getLong("transaction_id"), results.getString("trans_type"),
							toDollars(results.getLong("amount")), formatAccount(results, "account_src"),
							formatAccount(results, "account_dst"), results.getString("time_complete"));
				}
				if (!found) output.println("No transactions found.");
			}
		} catch (SQLException | BankException exception) {
			errorLogger.error("Could not read transaction history for account {}.", loggedInAccountId, exception);
			output.println("Transaction history is temporarily unavailable.");
		}
	}

	private void logOut() {
		loggedInAccountId = null;
		actionLogger.info("User logged out.");
		output.println("You have been logged out.");
	}

	private void printStartupScreen() {
		try (var startupScreen = BankCLI.class.getResourceAsStream(STARTUP_SCREEN)) {
			if (startupScreen == null) return;
			try (var reader = new Scanner(startupScreen)) {
				while (reader.hasNextLine()) output.println(reader.nextLine());
			}
		} catch (Exception exception) {
			errorLogger.error("Could not load the startup screen.", exception);
		}
	}

	private Long readAmount() {
		output.print("Amount ($): ");
		if (!scanner.hasNextLine()) {
			running = false;
			return null;
		}

		try {
			BigDecimal dollars = new BigDecimal(scanner.nextLine().trim()).setScale(2, RoundingMode.UNNECESSARY);
			return dollars.movePointRight(2).longValueExact();
		} catch (ArithmeticException | NumberFormatException exception) {
			output.println("Please enter a valid amount with no more than two decimal places.");
			return null;
		}
	}

	private Integer readInteger(String prompt) {
		Long value = readLong(prompt);
		if (value == null || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) return null;
		return value.intValue();
	}

	private Long readLong(String prompt) {
		output.print(prompt);
		if (!scanner.hasNextLine()) {
			running = false;
			return null;
		}

		try {
			return Long.parseLong(scanner.nextLine().trim());
		} catch (NumberFormatException exception) {
			output.println("Please enter a valid whole number.");
			return null;
		}
	}

	private void showError(BankException exception) {
		errorLogger.error("CLI operation failed: {}", exception.getMessage());
		output.println("Error: " + exception.getMessage());
	}

	private static double toDollars(long cents) {
		return cents / 100.0;
	}

	private static String formatAccount(java.sql.ResultSet results, String column) throws SQLException {
		long account = results.getLong(column);
		return results.wasNull() ? "-" : Long.toString(account);
	}
}

package com.bankofcli.api;

import java.io.PrintStream;
import java.util.Scanner;

public class BankCLI {

	private final Scanner scanner;
	private final PrintStream output;
	private boolean running;
	private Long loggedInAccountId;

	public BankCLI() {
		this(new Scanner(System.in), System.out);
	}

	public BankCLI(Scanner scanner, PrintStream output) {
		this.scanner = scanner;
		this.output = output;
	}

	/** Starts the terminal application. Business rules belong in the service layer. */
	public void run() {
		running = true;
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
		output.println("Registration will be connected to AccountService.");
	}

	private void logIn() {
		output.println("Login will be connected to AccountService.");
	}

	private void checkBalance() {
		output.println("Balance lookup will be connected to AccountService.");
	}

	private void deposit() {
		output.println("Deposit will be connected to TransactionService.");
	}

	private void withdraw() {
		output.println("Withdrawal will be connected to TransactionService.");
	}

	private void transfer() {
		output.println("Transfer will be connected to TransactionService.");
	}

	private void showTransactionHistory() {
		output.println("Transaction history will be connected to TransactionService.");
	}

	private void logOut() {
		loggedInAccountId = null;
		output.println("You have been logged out.");
	}
}

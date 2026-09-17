package com.bankofcli.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bankofcli.exception.BankException;
import com.bankofcli.exception.InvalidAccountIDException;
import com.bankofcli.exception.InvalidAmountException;
import com.bankofcli.exception.InvalidPinException;
import com.bankofcli.exception.NoTransactionHistoryException;
import com.bankofcli.exception.ServiceUnavailableException;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.TransactionService;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;


public class MainMenu extends BasicWindow{
	
	private final AccountRepository accountRepository = new AccountRepository();
	private final AccountService accountService = new AccountService(accountRepository);
	private final TransactionService transactionService = new TransactionService(accountRepository, new TransactionRepository());
	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
	private static final Logger transactionLogger = LoggerFactory.getLogger("Bank.Transaction.logback");
	SoundPlayer sounds= new SoundPlayer();
	Account curAccount;
	
	public MainMenu() {
		super("Main Menu");
	}
	
	/*
	 * Guest Menu setup
	 * */
	public void Guest() {
		this.setTitle("Main Menu");
		curAccount = null;
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		contentpane.addComponent(new Label("Welcome to The Bank of CLI"));
		contentpane.addComponent(new Button("Login",() -> this.Login()));
		contentpane.addComponent(new Button("Register",() -> this.register()));
		contentpane.addComponent(new Button("Exit",() ->this.close()));
		setComponent(contentpane);
	}
	
	/*
	 * Login Menu setup
	 * */
	public void Login() {
		this.setTitle("Login");
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Label AIDLabel = new Label("Account ID:");
		TextBox AID = new TextBox(new TerminalSize(30, 1));
		Label PinLabel = new Label("Pin:");
		TextBox Pin = new TextBox(new TerminalSize(30, 1)).setMask('*');
		content.addComponent(AIDLabel);
		content.addComponent(AID);
		content.addComponent(PinLabel);
		content.addComponent(Pin);
		content.addComponent(new Button("Login",() -> this.LoginHelper(AID.getText(),Pin.getText())));
		content.addComponent(new Button("Back", () -> this.Guest()));
		setComponent(content);
	}
	
	/*
	 * Method that actually completes Login
	 * */
	public void LoginHelper(String accountId, String pin) {
		actionLogger.info("Attempting to login user with Account ID: {}", accountId);
		long temp;
		int temppin;
		if (accountId.equals("")  || pin.equals("")) { 
			showError(new InvalidAccountIDException("Please enter valid Account ID and Pin"));
			return;
			}
		else {
			try {
				temp =Long.parseLong(accountId);
				temppin =Integer.parseInt(pin);
			}catch(Exception e){
				showError(new InvalidAccountIDException("Please enter valid Account ID and Pin"));
				return;
			}
		}
		try {
			curAccount =accountService.login(temp, temppin);
			actionLogger.info("User successfully logged in to account {}.", accountId);
			sounds.playSuccess();
			AccountMenu();
		} catch (BankException exception ) {
			showError(exception);
		}
	}

	/**
	 * Method used to create register screen
	 */
	public void register() {
		this.setTitle("Register");
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		TextBox pin = new TextBox(new TerminalSize(30, 1));
		Label pinlabel = new Label("Enter a four-digit PIN:");
		contentpane.addComponent(pinlabel);
		contentpane.addComponent(pin);
		contentpane.addComponent(new Button("Register", () -> registerConfirm(pin.getText())));
		setComponent(contentpane);
	}

	/**
	 * Method used to handle input of registering account
	 * @param pin the pin provided in text box
	 */
	public void registerConfirm(String pin) {
		this.setTitle("Register Confirmation");
		Panel contentpane = new Panel();
		int numpin;
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		try {
			numpin= Integer.parseInt(pin);
		} catch (Exception e) {
			showError(new InvalidPinException("Please Enter a four digit Pin number"));
			return;
		}
		
		try {
			Account account = accountService.register(numpin);
			actionLogger.info("User successfully registered account {}.", account.getAccountID());
			contentpane.addComponent(new Label("Registration successful. Your Account ID is: " + account.getAccountID()));
			contentpane.addComponent(new Label("Please store your Account ID number in a secure place."));
			contentpane.addComponent(new Button("back", () -> Guest()));
			setComponent(contentpane);
			sounds.playSuccess();
		} catch (BankException exception) {
			showError(exception);
		}
		
	}
	/*
	 * Account Menu setup
	 * */
	public void AccountMenu() {
		this.setTitle("AccountMenu");
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		contentpane.addComponent(new Button("Balance", ()->this.Balance()));
		contentpane.addComponent(new Button("Deposit", () -> Deposit()));
		contentpane.addComponent(new Button("Withdraw", () -> Withdraw()));
		contentpane.addComponent(new Button("Transfer", () -> transfer()));
		contentpane.addComponent(new Button("Transaction History", () -> transactionHistory()));
		contentpane.addComponent(new Button("Change Pin", () -> this.changePin()));
		contentpane.addComponent(new Button("Logout", () -> this.Guest()));
		contentpane.addComponent(new Button("Exit",() ->this.close()));
		setComponent(contentpane);
		
	}

	/**
	 * Method used to display the change pin screen
	 */
	public void changePin() {
		this.setTitle("ChangePin");
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		TextBox current = new TextBox(new TerminalSize(30, 1));
		TextBox newPin = new TextBox(new TerminalSize(30, 1));
		contentpane.addComponent(new Label("Current Pin:"));
		contentpane.addComponent(current);
		contentpane.addComponent(new Label("New Pin:"));
		contentpane.addComponent(newPin);
		contentpane.addComponent(new Button("Submit", () -> changePinHelper(newPin.getText(),current.getText())));
		setComponent(contentpane);
	}

	/**
	 * Method used to handle the input of the change pin screen
	 * @param newPinString current pin that user typed in
	 * @param currentPinString new pin user wishes to change it to
	 */
	public void changePinHelper(String newPinString, String currentPinString) {
		int newPin;
		int curPin;
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		
		try {
			newPin =Integer.parseInt(newPinString);
			curPin = Integer.parseInt(currentPinString);
		} catch (Exception e) {
			showErrorAccount(new InvalidPinException("Please Provide two valid Pin Numbers"));
			return;
		}
		
		try {
			accountService.changePin(curAccount.getAccountID(), curPin, newPin);
			actionLogger.info("PIN successfully changed for account {}.", curAccount.getAccountID());
			contentpane.addComponent(new Label("PIN successfully changed."));
			contentpane.addComponent(new Button("Back", () -> AccountMenu()));
			setComponent(contentpane);
			sounds.playSuccess();
		} catch (BankException exception) {
			showError(exception);
		}
		
		
	}

	/**
	 * Method used to display transfer screen
	 */
	public void transfer() {
		this.setTitle("Transfer");
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Label description = new Label("Destination AccountID:");
		Label descriptionamount = new Label("Amount to Send:");
		TextBox dest = new TextBox(new TerminalSize(30, 1));
		TextBox amount = new TextBox(new TerminalSize(30, 1));
		content.addComponent(description);
		content.addComponent(dest);
		content.addComponent(descriptionamount);
		content.addComponent(amount);
		content.addComponent(new Button("Send",() -> transferHelper(dest.getText(), amount.getText())));
		setComponent(content);
	}


	public void transferHelper(String destination, String num) {
		Long dest;
		Long amount;
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		try {
			dest =Long.parseLong(destination);
		} catch (Exception e) {
			showErrorAccount(new InvalidAccountIDException("Please enter a valid Destination Account"));
			return;
		}
		try {
			amount =parseAmount(num);
		}catch(InvalidAmountException iae) {
			showErrorAccount(iae);
			return;
		}
		
		try {
			transactionService.transfer(curAccount.getAccountID(), dest, amount);
			transactionLogger.info("Transfer completed from account {} to account {}: ${}.",
					curAccount.getAccountID(), dest, String.format("%,.2f",toDollars(amount)));
			String message = String.format("%nTransfer successful.New balance: $%,.2f%n",
					toDollars(accountService.getBalance(curAccount.getAccountID())));
			content.addComponent(new Label(message));
			content.addComponent(new Button("back", () -> AccountMenu()));
			sounds.playSuccess();
			setComponent(content);
		} catch (BankException exception) {
			showError(exception);
		}
		
		
		
	}
	public void Withdraw() {
		this.setTitle("Withdraw");
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Label num = new Label("How Much will you withdraw");
		TextBox HM= new TextBox(new TerminalSize(30, 1));
		content.addComponent(num);
		content.addComponent(HM);
		content.addComponent(new Button("Withdraw", ()->withdrawhelper(HM.getText())));
		
		setComponent(content);
	}
	public void withdrawhelper(String num) {
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Long amount;
		try {
			amount =parseAmount(num);
		}catch(InvalidAmountException iae) {
			showErrorAccount(iae);
			return;
		}
		try {
			transactionService.withdraw(curAccount.getAccountID(), amount);
			transactionLogger.info("Withdrawal completed for account {}: ${}.", curAccount.getAccountID(),
					String.format("%,.2f",toDollars(amount)));
			String message = String.format("\nWithdrawal successful. New balance: $%,.2f%n",
					toDollars(accountService.getBalance(curAccount.getAccountID())));
			content.addComponent(new Label(message));
			content.addComponent(new Button("back", () -> AccountMenu()));
			sounds.playSuccess();
			setComponent(content);
		} catch (BankException exception) {
			showErrorAccount(exception);
		}
		
	}
	public void Deposit() {
		this.setTitle("Deposit");
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Label num = new Label("How Much will you deposit");
		TextBox HM= new TextBox(new TerminalSize(30, 1));
		content.addComponent(num);
		content.addComponent(HM);
		content.addComponent(new Button("Deposit", ()->Deposithelper(HM.getText())));
		
		setComponent(content);
	}
	public void Deposithelper(String num) {
		Panel content = new Panel();
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		Long amount;
		try {
			
			amount =parseAmount(num);
		}catch(InvalidAmountException iae) {
			showErrorAccount(iae);
			return;
		}
		try {
			transactionService.deposit(curAccount.getAccountID(), amount);
			transactionLogger.info("Deposit completed for account {}: ${}.", curAccount.getAccountID(),
					String.format("%,.2f",toDollars(amount)));
			String message = String.format(
				    "%nDeposit successful. New balance: $%,.2f%n",
				    toDollars(accountService.getBalance(curAccount.getAccountID()))
				);
			content.addComponent(new Label(message));
			content.addComponent(new Button("back", () -> AccountMenu()));
			sounds.playSuccess();
			setComponent(content);
		} catch (BankException exception) {
			showErrorAccount(exception);
		}
		
		
	}
	public void Balance() {
		this.setTitle("Balance");
		Panel content = new Panel();
		String message = String.format(
				"\nCurrent balance: $%,.2f%n", toDollars(accountService.getBalance(curAccount.getAccountID()))
			);
		content.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		content.addComponent(new Label(message));
		content.addComponent(new Button("Back", () -> this.AccountMenu()));
		sounds.playSuccess();
		setComponent(content);
		
	}
	public void transactionHistory() {
		this.setTitle("Transaction History");
		Panel contentpane = new Panel();
		contentpane.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		try {
			List<Transaction> transactionList =
					transactionService.getTransactionHistory(curAccount.getAccountID(), 10);
			for (Transaction transaction: transactionList) {
				String message = String.format("Trans ID: %s | %s | $%,.2f | from %s to %s | %s%n",
						transaction.getTransactionID(), transaction.getType().toString(),
						toDollars(transaction.getAmount()), formatAccount(transaction.getAccountSrc()),
						formatAccount(transaction.getAccountDst()), transaction.getTimeComplete().toString());
				contentpane.addComponent(new Label(message));
			}
			setComponent(contentpane);
			contentpane.addComponent(new Button("Back", () -> AccountMenu()));
			sounds.playSuccess();
		} catch(NoTransactionHistoryException exception) {
			contentpane.addComponent(new Label("No Transactions found"));
			contentpane.addComponent(new Button("Back", () -> AccountMenu()));
			setComponent(contentpane);
			sounds.playFailure();
		}
		catch (SQLException exception) {
			errorLogger.error("Could not read transaction history for account {}.", curAccount.getAccountID(), exception);
			showErrorAccount(new ServiceUnavailableException("Transaction history is temporarily unavailable.", exception));
        }
		
	}
	public void showError(BankException exception) {
		errorLogger.error("CLI operation failed: {}", exception);
		Panel Errorcontent = new Panel();
		Errorcontent.addComponent(new Label(exception.getMessage()));
		Errorcontent.addComponent(new Button("Back",() -> this.Login()));
		setComponent(Errorcontent);
		sounds.playFailure();
	}
	public void showErrorAccount(BankException exception) {
		errorLogger.error("CLI operation failed: {}", exception);
		Panel Errorcontent = new Panel();
		Errorcontent.addComponent(new Label(exception.getMessage()));
		Errorcontent.addComponent(new Button("Back",() -> this.AccountMenu()));
		setComponent(Errorcontent);
		sounds.playFailure();
	}
	/** Converts extended cents format to dollars */
	private static double toDollars(long cents) {
		return cents / 100.0;
	}
	/* Used to format how empty accounts are displayed on transaction history for
	* source or destination accounts. */
	private static String formatAccount(Long accountID){
		return accountID == null ? "-" : accountID.toString();
	}
	/**
	 * When handling transfers, withdraws and deposits, takes in the user's input,
	 * verifies it and converts into Long extended cents format ($10.00 = 1000L)
	 * @return Long extended cents format ($10.00 = 1000L)
	 */
	private Long parseAmount(String input) {
		try {
			BigDecimal dollars = new BigDecimal(input.trim()).setScale(2, RoundingMode.UNNECESSARY);
			return dollars.movePointRight(2).longValueExact();
		} catch (ArithmeticException | NumberFormatException exception) {
			throw new InvalidAmountException(
					"Please enter a valid amount with no more than two decimal places.");
		}
	}

}

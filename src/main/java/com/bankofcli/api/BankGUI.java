package com.bankofcli.api;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Main window for GUI of application
public class BankGUI {

	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
	DefaultTerminalFactory terminalFactory;
	Screen screen;
	private final String WINDOW_TITLE = "Bank of CLI";
	private final int COLUMNS = 120;
	private final int ROWS = 40;

	public BankGUI() {
		terminalFactory = new DefaultTerminalFactory();
		terminalFactory.setTerminalEmulatorTitle(WINDOW_TITLE);
		screen = null;
	}

	//Creates the screen for the application and runs the program from the window
	public void run(){

		try {
			actionLogger.info("Starting up GUI view.");
			//Initial Setup of terminal, screen and Window, with MainMenu object
			// Sets the initial terminal grid dimensions (columns x rows)
			terminalFactory.setInitialTerminalSize(new TerminalSize(COLUMNS, ROWS));
			screen = terminalFactory.createScreen();
			screen.startScreen();

			// Show Splash Screen
			SplashScreen splash = new SplashScreen(screen);
			splash.display();

			final WindowBasedTextGUI WindowManager = new MultiWindowTextGUI(screen);

			final MainMenu mainWindow = new MainMenu();
			mainWindow.Guest();
			WindowManager.addWindowAndWait(mainWindow);


		}catch(IOException e) {
			errorLogger.error("An exception has occurred while attempting to run the application: {}", e.getMessage());
		}
		finally {
			if(screen != null) {
				try {
                    /*
                    The close() call here will restore the terminal by exiting from private mode
                     */
					screen.close();
					actionLogger.info("GUI view was exited successfully, closing program.");
				}
				catch(IOException e) {
					errorLogger.error("An exception has occurred while attempting to close the application: {}", e.getMessage());
				}
			}
		}
	}
}

package com.bankofcli.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.DelegatingTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

//Main window for GUI of application
public class BankGUI {

	private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");
	private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
	DefaultTerminalFactory terminalFactory;
	Screen screen;
	private final String WINDOW_TITLE = "Bank of CLI";
	private final int COLUMNS = 120;
	private final int ROWS = 40;
	private SoundPlayer sounds;
	
	public BankGUI() {
		terminalFactory = new DefaultTerminalFactory();
		terminalFactory.setTerminalEmulatorTitle(WINDOW_TITLE);
		screen = null;
		sounds= new SoundPlayer();
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
			//Adds a window listener to the window to intercept every key pressed before it is handled by the GUI
			mainWindow.addWindowListener(new WindowListenerAdapter() {
				
				@Override
				public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
					if (isValidKey(keyStroke)) {
						sounds.playClick();
					}
				}
			});
			Theme theme =LanternaThemes.getRegisteredTheme("blaster");
			//wrapper for predefined Theme to make individual adjustments to the Theme
			DelegatingTheme modifiedTheme = new DelegatingTheme(theme);
			WindowManager.setTheme(modifiedTheme);
			
			mainWindow.setHints(Arrays.asList(Window.Hint.CENTERED));
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
	
	
    private static boolean isValidKey(KeyStroke keyStroke) {
        KeyType type = keyStroke.getKeyType();
        if (type == KeyType.Character) {
            return Character.isLetterOrDigit(keyStroke.getCharacter());
        }
        return type == KeyType.ArrowUp ||
               type == KeyType.ArrowDown ||
               type == KeyType.ArrowLeft ||
               type == KeyType.ArrowRight ||
               type == KeyType.Enter;
    }
	
	
}

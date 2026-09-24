package com.bankofcli.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
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

import ComponentThemeDefinitions.Patterned_backdrops;
import ComponentThemeDefinitions.gorgonDelegatingTheme;

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
    private final static TextColor bg= new TextColor.RGB(42, 123, 76);
    private final TextColor fg =  new TextColor.RGB(212, 175, 55);
    private final TextColor textBg = new TextColor.RGB(163, 169, 166);
    private final TextColor windowColor = new TextColor.RGB(237, 203, 142);
	
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

			final WindowBasedTextGUI WindowManager = new MultiWindowTextGUI(screen, new DefaultWindowManager(),new Patterned_backdrops());

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
			Theme theme =LanternaThemes.getRegisteredTheme("conqueror");
			//wrapper for predefined Theme to make individual adjustments to the Theme
			gorgonDelegatingTheme modifiedTheme = new gorgonDelegatingTheme(theme);
			
			//Space for overidding individual component themes
			SimpleTheme buttonStyle = SimpleTheme.makeTheme(
					true,TextColor.ANSI.BLACK,new TextColor.RGB(237, 203, 142),fg,new TextColor.RGB(0, 0, 0),fg,TextColor.ANSI.WHITE,bg
					);
			
			SimpleTheme panelStyle = SimpleTheme.makeTheme(
				    true,
				    textBg,             
				    windowColor,    
				    textBg,             
				    windowColor,    
				    textBg,            
				    windowColor,              
				    TextColor.ANSI.RED                
				);
			SimpleTheme textBoxStyle = SimpleTheme.makeTheme(true, TextColor.ANSI.BLACK, fg, TextColor.ANSI.BLACK, windowColor, TextColor.ANSI.WHITE, textBg, TextColor.ANSI.BLACK);
			
			SimpleTheme borderStyle = SimpleTheme.makeTheme(
				    true,
				    TextColor.ANSI.BLACK,             
				    windowColor,    
				    textBg,             
				    windowColor,    
				    TextColor.ANSI.BLACK,            
				    windowColor,              
				    TextColor.ANSI.RED   
				);
			SimpleTheme labelStyle= SimpleTheme.makeTheme(true, TextColor.ANSI.BLACK, windowColor, TextColor.ANSI.BLACK, windowColor, TextColor.ANSI.WHITE, textBg, bg);
			
			modifiedTheme.override(Label.class, labelStyle.getDefaultDefinition());
			modifiedTheme.override(Border.class, borderStyle.getDefaultDefinition());
			modifiedTheme.override(Panel.class, panelStyle.getDefaultDefinition());
			modifiedTheme.override(DefaultWindowDecorationRenderer.class, borderStyle.getDefaultDefinition());
			modifiedTheme.override(Button.class,buttonStyle.getDefaultDefinition());
			modifiedTheme.override(TextBox.class, textBoxStyle.getDefaultDefinition());

			
			
			
			WindowManager.setTheme(modifiedTheme);
			
			mainWindow.setHints(Arrays.asList(Window.Hint.CENTERED,Window.Hint.NO_POST_RENDERING));
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

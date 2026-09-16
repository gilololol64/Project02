package com.bankofcli.api;

import java.io.IOException;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class BankGUI {
	
	
	public BankGUI() {
		DefaultTerminalFactory  terminalFactory = new DefaultTerminalFactory();
		Screen screen = null;
		
		try {
			//Initial Setup of terminal, screen and Window, with MainMenu object
			screen = terminalFactory.createScreen();
            screen.startScreen();
            
            final WindowBasedTextGUI WindowManager = new MultiWindowTextGUI(screen);
            
            final MainMenu mainWindow = new MainMenu();
            mainWindow.Guest();
            WindowManager.addWindowAndWait(mainWindow);
            
			
		}catch(IOException e) {
			
		}
		finally {
			if(screen != null) {
                try {
                    /*
                    The close() call here will restore the terminal by exiting from private mode
                     */
                    screen.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
			}
		}
		
	}
}

package com.bankofcli.api;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Splash Screen / Start up for Application
 */
public class SplashScreen extends MultiWindowTextGUI {

    private static final String STARTUP_SCREEN = "/text_graphics/startUpScreenText.txt";
    private static final Logger actionLogger = LoggerFactory.getLogger("AccountAction");
    private static final Logger errorLogger = LoggerFactory.getLogger("Bank.logback.Error");
    private static final List<String> ASCII_ART = new ArrayList<>();
    private final Screen screen;
    private final int LINE_SPEED = 160;
    private final int PROMPT_WAIT = 550;
    private final SoundPlayer soundPlayer;

    public SplashScreen(Screen screen){
        super(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        this.screen = screen;

        readStartupScreen();

        SimpleTheme theme = SimpleTheme.makeTheme(false,
                TextColor.ANSI.BLUE, TextColor.ANSI.BLUE, TextColor.ANSI.BLUE, TextColor.ANSI.BLUE,
                TextColor.ANSI.BLUE, TextColor.ANSI.BLUE, TextColor.ANSI.BLUE);
        setTheme(theme);

        soundPlayer = new SoundPlayer();
    }

    /**
     * Displays splash screen drawing art line by line with startup sound
     */
    public void display() throws IOException {

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel artPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        mainPanel.addComponent(artPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));

        BasicWindow splashWindow = new BasicWindow();
        splashWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS));
        splashWindow.setComponent(mainPanel);

        addWindow(splashWindow);

        // Spawn a background thread for animation to let GUI repaint each frame
        Thread animationThread = createAnimationThread(artPanel);

        animationThread.start();

        // Block main application execution until keypress
        screen.readInput();

        // Interrupt thread if user skips early via keypress
        animationThread.interrupt();
        splashWindow.close();
    }

    /**
     * Creates thread to draw animation and play start up sound
     * @param artPanel panel that the thread is drawing to
     * @return a thread with the animation process defined.
     */
    private Thread createAnimationThread(Panel artPanel){
        actionLogger.info("Beginning splash screen animation");
        return new Thread(() -> {
            soundPlayer.playStartUp();
            try {
                for (String line : ASCII_ART) {
                    Label label = new Label(line);
                    label.setForegroundColor(TextColor.ANSI.WHITE);
                    artPanel.addComponent(label);
                    updateScreen();
                    Thread.sleep(LINE_SPEED);
                }

                // Append prompt after art finishes rendering
                artPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
                Thread.sleep(PROMPT_WAIT);
                Label prompt = new Label("Press any key to continue...");
                prompt.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
                artPanel.addComponent(prompt.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));

                updateScreen();
            } catch (IOException | InterruptedException e) {
                errorLogger.error("Could not properly draw splash screen.");
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Reads ASCII ART of the start/splash screen of application
     */
    private void readStartupScreen() {

        try (var startupScreen = SplashScreen.class.getResourceAsStream(STARTUP_SCREEN)) {
            if (startupScreen == null) return;
            try (var reader = new Scanner(startupScreen)) {
                while (reader.hasNextLine()) {
                    ASCII_ART.add(reader.nextLine());
                }
            }
        } catch (Exception exception) {
            errorLogger.error("Could not load the splash screen.", exception);
        }
    }
}

// Authors:
// Software Developer: OrkhanGG
// API Developer: WinChester1723
// UI Developer: Deusrazen

import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import gui.UICore;
import gui.controls.GMenuBar;
import gui.frames.GUIFrame;
import gui.panels.TranslateUIPanel;
import io.InputCore;

import javax.swing.*;
import java.io.IOException;


public class Application {

    public static void main(String[] args) throws IOException {
        // Start GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try { // TODO: Move to respective places
                    UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme());
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }

                //IntelliJTheme.setup(getClass().getResourceAsStream("/template.theme.json"));

                final UICore uiCore = UICore.getInstance();
                uiCore.setMainUIPanel(new TranslateUIPanel());
                uiCore.setMainUIFrame(new GUIFrame<>(JFrame.class, UICore.getInstance().getMainUIPanel()));
                uiCore.appendFrame("Main", UICore.getInstance().getMainUIFrame());
                uiCore.getMainUIFrame().Initialize();
                uiCore.getMainUIFrame().setOnCloseCallback(()->{System.exit(1);});// Call this function on close
                uiCore.getMainUIFrame().getAppFrame().setJMenuBar(new GMenuBar());

                SwingUtilities.updateComponentTreeUI((JFrame)uiCore.getMainUIFrame().getAppFrame());
            }
        });

        // Implement Input Core
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new InputCore());
    }
}
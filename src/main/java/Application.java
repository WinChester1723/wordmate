// Authors:
// Software Developer: OrkhanGG
// API Developer: WinChester1723
// UI Developer: Deusrazen

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import gui.frames.TranslateUIPanel;
import io.InputCore;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.IOException;


public class Application {

    public static void main(String[] args) throws IOException {
        // Start GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    TranslateUIPanel.getInstance().Initialize();

                    UIManager.setLookAndFeel(new FlatDarkLaf());

                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }
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
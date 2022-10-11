// Authors: OrkhanGG, WinChester1723, Deusrazen

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import gui.core.TranslateUI;
import io.InputCore;

import javax.swing.text.BadLocationException;
import java.io.IOException;


public class Application {

    public static void main(String[] args) throws IOException {
        // Start GUI
        try {
            TranslateUI.getInstance().Initialize();
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

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
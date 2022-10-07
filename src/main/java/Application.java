package main.java;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import main.java.gui.GUICore;
import main.java.io.keyboard.InputCore;

import java.io.IOException;


public class Application {

    public static void main(String[] args) throws IOException {

        // Start GUI
        GUICore.getInstance().InitGUI();

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
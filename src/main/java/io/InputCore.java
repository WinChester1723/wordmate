package io;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.platform.win32.User32;
import gui.frames.TranslateUIPanel;
import utils.ClipboardManager;

import javax.swing.*;

public final class InputCore implements NativeKeyListener {//<-- Remember to add the jnativehook library

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_HOME) {
            try {
                TranslateUIPanel.getInstance().updateInputField(ClipboardManager.getInstance().getSelectedText(User32.INSTANCE, ClipboardManager.CustomUser32.INSTANCE));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            JFrame mf = TranslateUIPanel.getInstance().getParentFrame();

            int state = mf.getExtendedState();
            state &= ~JFrame.ICONIFIED;
            mf.setExtendedState(state);
            mf.setAlwaysOnTop(true);
            mf.toFront();
            mf.requestFocus();
            mf.setAlwaysOnTop(false);
        }
    }

//    public void nativeKeyReleased(NativeKeyEvent e) {
//        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//    }
//
//    public void nativeKeyTyped(NativeKeyEvent e) {
//        System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//    }
}

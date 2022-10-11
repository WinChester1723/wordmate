package io;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.platform.win32.User32;
import gui.core.TranslateUI;
import main.java.gui.frames.MiniQuick;
import utils.ClipboardManager;

import javax.swing.*;

public final class InputCore implements NativeKeyListener {//<-- Remember to add the jnativehook library

    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (e.getKeyCode() == NativeKeyEvent.VC_HOME) {
            try {
                //System.out.println(ClipboardManager.getInstance().getSelectedText(User32.INSTANCE, ClipboardManager.CustomUser32.INSTANCE));
                TranslateUI.getInstance().updateInputField(ClipboardManager.getInstance().getSelectedText(User32.INSTANCE, ClipboardManager.CustomUser32.INSTANCE));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            JFrame mf = TranslateUI.getInstance().getTranslateUIFrame();

            int state = mf.getExtendedState();
            state &= ~JFrame.ICONIFIED;
            mf.setExtendedState(state);
            mf.setAlwaysOnTop(true);
            mf.toFront();
            mf.requestFocus();
            mf.setAlwaysOnTop(false);
        }

        if(e.getKeyCode() == NativeKeyEvent.VC_F4){
            String text = null;
            try {
                //System.out.println(ClipboardManager.getInstance().getSelectedText(User32.INSTANCE, ClipboardManager.CustomUser32.INSTANCE));
                text = ClipboardManager.getInstance().getSelectedText(User32.INSTANCE, ClipboardManager.CustomUser32.INSTANCE);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if(text.equals(null))
                return;

            JFrame mf = MiniQuick.getInstance().getMiniQuickFrame();

            MiniQuick.getInstance().setText(text);
            MiniQuick.getInstance().show(true);

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

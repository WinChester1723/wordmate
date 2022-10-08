package main.java.gui.frames;

// This frame will pop up when it's called by special key(or key combinations)
// And translate the selected text depending on user's configuration(from which language to another)

import javax.swing.*;
import java.awt.*;

public class MiniQuick {

    private static MiniQuick miniQuick = null;

    public static MiniQuick getInstance(){
        if(miniQuick == null)
            return miniQuick = new MiniQuick();

        return miniQuick;
    }

    private static JFrame miniQuickFrame = null;
    private static JPanel miniQuickPanel = null;
    private static Label translatedText = null;
    private MiniQuick(){
        miniQuickFrame = new JFrame();
        miniQuickPanel = new JPanel();

        miniQuickPanel.setLayout(new BorderLayout());
        miniQuickPanel.add(translatedText);

        miniQuickFrame.setLayout(new BorderLayout());
        miniQuickFrame.add(miniQuickPanel);

        miniQuickFrame.pack();

        java.awt.event.FocusListener myFocusListener = new java.awt.event.FocusListener() {
            public void focusGained(java.awt.event.FocusEvent focusEvent) {
                try {
                    //JTextField src = (JTextField)focusEvent.getSource();
                    miniQuickFrame.setVisible(true);
                } catch (ClassCastException ignored) {
                    /* I only listen to JTextFields */
                }
            }

            public void focusLost(java.awt.event.FocusEvent focusEvent) {
                try {
                    //JTextField src = (JTextField)focusEvent.getSource();
                    miniQuickFrame.setVisible(false);
                } catch (ClassCastException ignored) {
                    /* I only listen to JTextFields */
                }
            }
        };

        miniQuickFrame.addFocusListener(myFocusListener);
    }

    public static JFrame getMiniQuickFrame() {
        return miniQuickFrame;
    }

    public void show(boolean show){
        miniQuickFrame.setVisible(show);
    }

    public void setText(String text){
        translatedText.setText(text);
    }

}

package gui.dialogs;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import gui.frames.TranslateUIPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AppearanceDialog extends JDialog {

    public AppearanceDialog() {
        setLayout(new GridLayout(2, 2));
        List<JButton> themeButtons = new ArrayList<>();
        themeButtons.add(new JButton("Light"));
        themeButtons.add(new JButton("Silver"));
        themeButtons.add(new JButton("Dark"));
        themeButtons.add(new JButton("Carbon"));

        for (JButton button : themeButtons) {
            add(button);
            button.addActionListener(e -> {
                try {
                    final String btnName = button.getText();
                    switch (btnName) {
                        case "Light" -> UIManager.setLookAndFeel(new FlatLightLaf());
                        case "Silver" -> UIManager.setLookAndFeel(new FlatIntelliJLaf());
                        case "Dark" -> UIManager.setLookAndFeel(new FlatDarkLaf());
                        case "Carbon" -> UIManager.setLookAndFeel(new FlatDarculaLaf());
                    }
                } catch (UnsupportedLookAndFeelException ex) {
                    throw new RuntimeException(ex);
                }

                SwingUtilities.updateComponentTreeUI(TranslateUIPanel.getInstance().getParentFrame());
                SwingUtilities.updateComponentTreeUI(this);
                dispose();
            });
        }
        setSize(new Dimension(300,200));
        setResizable(false);
        setModal(true);
        setVisible(true);
    }

}

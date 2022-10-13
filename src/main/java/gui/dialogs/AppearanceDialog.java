package gui.dialogs;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import gui.controls.TitleBar;
import gui.frames.TranslateUIPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class AppearanceDialog extends JPanel{
    private TitleBar<JDialog> ParentFrame = null;
    private List<JButton> themeButtons = null;
    private JPanel themePanel = null;
    private JSlider opacity = null;
    public AppearanceDialog() {
        themePanel = new JPanel();
        themePanel.setLayout(new GridLayout(2, 2));
        themeButtons = new ArrayList<>();
        themeButtons.add(new JButton("Light"));
        themeButtons.add(new JButton("Silver"));
        themeButtons.add(new JButton("Dark"));
        themeButtons.add(new JButton("Carbon"));

        for(var button : themeButtons)
            themePanel.add(button);

        opacity = new JSlider(JSlider.HORIZONTAL,30,100,100);

        Hashtable<Integer, JLabel> labelTable =
                new Hashtable<Integer, JLabel>();
        labelTable.put(30, new JLabel("Transparent") );
        labelTable.put(100, new JLabel("Opaque") );
        opacity.setLabelTable(labelTable);
        opacity.setPaintLabels(true);

        setLayout(new BorderLayout());
        add(themePanel, BorderLayout.PAGE_START);
        add(opacity, BorderLayout.PAGE_END);
        setSize(new Dimension(300,200));

        addListeners();

        ParentFrame = new TitleBar<>(JDialog.class,this);
        ParentFrame.Initialize();
    }

    private void addListeners(){
        opacity.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                final float op = opacity.getValue()/100.f;
                TranslateUIPanel.getInstance().getParentFrame().setOpacity(op);
                ParentFrame.getAppFrame().setOpacity(op);
            }
        });

        for (JButton button : themeButtons) {
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
                ParentFrame.getAppFrame().dispose();
            });
        }
    }

}

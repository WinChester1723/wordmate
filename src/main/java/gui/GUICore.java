package main.java.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import main.java.io.translator.TranslationCore;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;

public final class GUICore {
    // Constants
    final static short translateFieldMaxLength = 500;
    private static GUICore single_instance = null;
    // Main GUI Elements
    private JFrame mainFrame = null;
    private JPanel mainPanel = null;
    // INFO: Translate Section
    private JPanel translateFromPreferencePanel = null;
    private GridLayout translateFromPreferencePanelGL = null;
    private JPanel translateFromUserLanguagesPanel = null;
    private ArrayList<JToggleButton> translateFromUserLanguages = null;
    private JButton swapLanguages = null;
    private JComboBox translateFromLanguageDropdown = null;
    private JTextArea translateFromField = null;
    // INFO: TranslateTo Section
    private JPanel translateToPreferencePanel = null;
    private GridLayout translateToPreferencePanelGL = null;
    private JPanel translateToUserLanguagesPanel = null;
    private ArrayList<JToggleButton> translateToUserLanguages = null;
    private JTextArea translateToField = null;
    private JComboBox translateToLanguageDropdown = null;

    // Side-Thread(s) // TODO: Move from this class.
    private Thread translatorThread = null;

    // Public Methods
    public static GUICore getInstance() {
        if (single_instance == null) single_instance = new GUICore();

        return single_instance;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    // INFO: RequestTranslate will do some pre-checks and set the translation area text if everything's okay.
    //  As the translation area text has a listener, it'll do translation job itself, we don't need to call anything else.
    public void RequestTranslate(String textToTranslate) {
        if (textToTranslate.length() >= translateFieldMaxLength) {
            JOptionPane.showMessageDialog(null, "Given text cannot be greated than 500 characters long!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        translateFromField.setText(textToTranslate);
    }

    public void InitGUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println(e);
        }

        // Init UI Elements
        {
            mainFrame = new JFrame();
            mainPanel = new JPanel();
        }

        // Translate From Initialization
        {
            translateFromPreferencePanel = new JPanel();
            translateFromUserLanguagesPanel = new JPanel();//--------
            translateFromPreferencePanelGL = new GridLayout(1, 3);
            translateFromUserLanguages = new ArrayList<>();
            translateFromUserLanguages.add(new JToggleButton("Detect Language"));
            translateFromUserLanguages.add(new JToggleButton("English"));
            translateFromUserLanguages.add(new JToggleButton("Russian"));
            translateFromUserLanguages.add(new JToggleButton("French"));//-------
            translateFromLanguageDropdown = new JComboBox();
            swapLanguages = new JButton("Swap");
            translateFromField = new JTextArea("");
            //----------
            translateToPreferencePanel = new JPanel();
            translateToUserLanguagesPanel = new JPanel();//--------
            translateToPreferencePanelGL = new GridLayout(1, 3);
            translateToUserLanguages = new ArrayList<>();
            translateToUserLanguages.add(new JToggleButton("English"));
            translateToUserLanguages.add(new JToggleButton("Russian"));
            translateToUserLanguages.add(new JToggleButton("French"));//-------
            translateToLanguageDropdown = new JComboBox();
            translateToField = new JTextArea("");
        }

        // TranslateFromPreferencePanel Handling
        {
            translateFromUserLanguagesPanel.setLayout(translateFromPreferencePanelGL);
            for (var i : translateFromUserLanguages)// add checkboxes to the panel
                translateFromUserLanguagesPanel.add(i);

            // TranslateFromPreferencePanel Handling
            translateFromPreferencePanel.add(translateFromUserLanguagesPanel);
            translateFromPreferencePanel.add(swapLanguages);
            translateFromPreferencePanel.add(translateFromLanguageDropdown);
        }

        // TranslateFromPreferencePanel Handling
        {
            translateToUserLanguagesPanel.setLayout(translateToPreferencePanelGL);
            for (var i : translateToUserLanguages)// add checkboxes to the panel
                translateToUserLanguagesPanel.add(i);

            // TranslateFromPreferencePanel Handling
            translateToPreferencePanel.add(translateToUserLanguagesPanel);
            translateToPreferencePanel.add(translateToLanguageDropdown);
        }

        // TranslateOptions Handle
        {
            // TODO: Remove this functionality.
            //  Since we've switched up google like 4 button system to choose language to translate.
            translateFromLanguageDropdown.setSelectedItem("English");// TO-DO: Load previously selected values
            translateToLanguageDropdown.setSelectedItem("Turkish");// TO-DO: Load previously selected values
        }

        // Handle Translate Field Configuration
        {
            translateFromField.setLineWrap(true);
            translateFromField.setWrapStyleWord(true);
            translateFromField.setDocument(new JTextFieldLimit(translateFieldMaxLength));
            //---------
            translateToField.setLineWrap(true);
            translateToField.setWrapStyleWord(true);
            translateToField.setEditable(false);
        }

        // TODO: Get languages by Google if there's any internet connection.(Keep the existing database though!)
        LoadSupportedLanguages();// We have a small database that contains a Language map.

        AddListeners();// Add listeners for the controls that exist in this function

        // Final touches
        {
            mainPanel.setLayout(new GridLayout(2, 2));

            mainPanel.add(translateFromPreferencePanel);
            mainPanel.add(translateToPreferencePanel);
            mainPanel.add(translateFromField);
            mainPanel.add(translateToField);

            mainFrame.add(mainPanel);
            mainFrame.setAlwaysOnTop(false);
            mainFrame.pack();
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
//        if (!SystemTray.isSupported()) {
//            System.out.println("SystemTray is not supported");
//            return;
//        }
//        Image image = Toolkit.getDefaultToolkit().getImage("MY/PATH/TO_IMAGE");
//
//        final PopupMenu popup = new PopupMenu();
//        final TrayIcon trayIcon = new TrayIcon(image, "MY PROGRAM NAME", popup);
//        final SystemTray tray = SystemTray.getSystemTray();
//
//        MenuItem exitItem = new MenuItem("Exit");
//        exitItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.exit(1);
//            }
//        });
//        popup.add(exitItem);
//
//        trayIcon.setPopupMenu(popup);
//
//        try {
//            tray.add(trayIcon);
//        } catch (AWTException e) {
//            System.out.println("TrayIcon could not be added.");
//        }
    }

    private void LoadSupportedLanguages() {
        for (var i : TranslationCore.getInstance().getAvailableLanguages().entrySet()) {
            translateFromLanguageDropdown.addItem(i.getKey());
            translateToLanguageDropdown.addItem(i.getKey());
        }
    }

    private void AddListeners() {
        translateFromLanguageDropdown.addItemListener(new ItemListener() {
            String previousItem = "";

            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    previousItem = (String) event.getItem();
                }

                if (event.getStateChange() == ItemEvent.SELECTED) {
                    if (event.getItem().toString().equals(translateToLanguageDropdown.getSelectedItem().toString())) {
                        translateToLanguageDropdown.setSelectedItem(previousItem);

                        // Swap values
                        String translateFieldText = translateFromField.getText();
                        String translatedFieldText = translateToField.getText();
                        translateFromField.setText(translatedFieldText);
                        translateToField.setText(translateFieldText);
                    }
                }
            }
        });
        translateToLanguageDropdown.addItemListener(new ItemListener() {
            String previousItem = "";

            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    previousItem = (String) event.getItem();
                }

                if (event.getStateChange() == ItemEvent.SELECTED) {
                    if (event.getItem().toString().equals(translateFromLanguageDropdown.getSelectedItem().toString())) {
                        translateFromLanguageDropdown.setSelectedItem(previousItem);

                        // Swap values
                        String translateFieldText = translateFromField.getText();
                        String translatedFieldText = translateToField.getText();
                        translateFromField.setText(translatedFieldText);
                        translateToField.setText(translateFieldText);
                    }
                }
            }
        });
        translateFromField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

                translatorThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //translatorThread.wait(500);

                            final String from = translateFromLanguageDropdown.getItemAt(translateFromLanguageDropdown.getSelectedIndex()).toString();
                            final String to = translateToLanguageDropdown.getItemAt(translateToLanguageDropdown.getSelectedIndex()).toString();

                            final String translatedText = TranslationCore.getInstance().translate(TranslationCore.getInstance().getLanguageCodeByName(from), TranslationCore.getInstance().getLanguageCodeByName(to), translateFromField.getText());

                            translateToField.setText(translatedText);
                        } catch (IOException exc) {
                            System.out.println(exc);
                        }
                    }
                });
                translatorThread.start();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    // Nested Classes
    private final class JTextFieldLimit extends PlainDocument {
        private final int limit;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        JTextFieldLimit(int limit, boolean upper) {
            super();
            this.limit = limit;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }
}
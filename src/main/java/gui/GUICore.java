package main.java.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import main.java.io.translator.TranslationCore;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GUICore {
    // Constants
    final static short translateFieldMaxLength = 500;
    private static GUICore single_instance = null;
    // info: ------------------------------------
    // Main GUI Elements
    private JFrame mainFrame = null;
    private JPanel mainPanel = null;
    // INFO: Translate Section
    private JPanel translateFromPreferencePanel = null;
    private GridLayout translateFromPreferencePanelGL = null;
    private JPanel translateFromUserLanguagesPanel = null;
    private Map<String, JToggleButton> translateFromUserLanguages = null;
    private JButton swapLanguages = null;
    private JComboBox translateFromLanguageDropdown = null;
    private JTextArea translateFromField = null;
    // INFO: TranslateTo Section
    private JPanel translateToPreferencePanel = null;
    private GridLayout translateToPreferencePanelGL = null;
    private JPanel translateToUserLanguagesPanel = null;
    private Map<String, JToggleButton> translateToUserLanguages = null;
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
    public boolean UpdateTranslateFromField(String textToTranslate) {
        if (textToTranslate.length() >= translateFieldMaxLength) {
            JOptionPane.showMessageDialog(null, "Given text cannot be greated than 500 characters long!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        translateFromField.setText(textToTranslate);

        return true;
    }

    private void loadLanguagesButtons() {
        boolean saveFound = false;
        if (!saveFound) { // Default initialization values
            // left side
            translateFromUserLanguages.put("English", new JToggleButton("English"));
            translateFromUserLanguages.put("Russian", new JToggleButton("Russian"));
            translateFromUserLanguages.put("French", new JToggleButton("French"));//-------
            setSelectedLanguage("English", TranslationSides.TS_LEFT);
            // right side
            translateToUserLanguages.put("Turkish", new JToggleButton("Turkish"));
            translateToUserLanguages.put("Spanish", new JToggleButton("Spanish"));
            translateToUserLanguages.put("Japanese", new JToggleButton("Japanese"));//-------
            setSelectedLanguage("Turkish", TranslationSides.TS_RIGHT);
        }

        // TODO: Serialize user info to load on startup.
    }

    private String[] getExistingLanguages(TranslationSides side) {
        final int buttonCount = 3;
        String[] out = new String[buttonCount];
        for (int c = 0; c < buttonCount; c++)
            out[c] = getLanguageButtonsBySide(side).get(c).getText();
        return out;
    }

    private Map<String, JToggleButton> getLanguageButtonsBySide(TranslationSides side) {
        if (side.equals(TranslationSides.TS_LEFT)) return translateFromUserLanguages;
        else return translateToUserLanguages;
    }

    // Returns the selected language button's value; Returns null if could not find any selected button
    private String getSelectedLanguage(TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side).entrySet()) {
            if (i.getValue().isSelected()) return i.getKey();
        }
        return null;
    }

    // Returns the selected language button's index
    private int getSelectedLanguageByIndex(TranslationSides side) {
        for (int c = 0; c < getLanguageButtonsBySide(side).size(); c++)
            if (getLanguageButtonsBySide(side).get(getLanguageButtonsBySide(side).keySet().toArray()[c]).isSelected())
                return c;
        return -1;
    }

    // Attempts to select the given language.
    private void setSelectedLanguage(String lang, TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side).entrySet())
                i.getValue().setSelected(false);
        getLanguageButtonsBySide(side).get(lang).setSelected(true);
    }

    private boolean containsLanguage(String lang, TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side).entrySet())
            if (i.getValue().equals(lang)) return true;
        return false;
    }

    private void addLanguage(String lang, TranslationSides side) {
        if (!containsLanguage(lang, side)) {
            // We are going to insert a new language
            // Since we always will have 3 languages -> EN, RU, AZ
            // We can push the first and remove the last -> FR, EN, RU
            String[] _languages_ = getExistingLanguages(side);
            for (int c = _languages_.length - 1; c >= 1; c--) {
                _languages_[c] = _languages_[c - 1];
            }
            _languages_[0] = lang;
            //---------------------------------------------------------

            var temporary = getLanguageButtonsBySide(side);
            getLanguageButtonsBySide(side).clear();
            // Since we are not able to change the keys easily
            // We are going to store buttons in temporary variable[so we may not add them again to the Layout]
            // And re-add them into the list with new keys.
            for (int c = 0; c < _languages_.length; c++) {
                var button = temporary.get(temporary.keySet().toArray()[c]);
                button.setText(_languages_[c]);
                getLanguageButtonsBySide(side).put(_languages_[c], button);
            }
        }
        // Finally, whether it's recently added or not, select the button
        setSelectedLanguage(lang, side);
    }

    public void RequestToSelectLanguage(String lang, TranslationSides side) {
        if (containsLanguage(lang, side)) {
            setSelectedLanguage(lang, side);
        } else {
            addLanguage(lang, side);
        }
    }

    public void Initialize() throws BadLocationException {
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
            translateFromUserLanguages = new LinkedHashMap<>();
            translateFromLanguageDropdown = new JComboBox();
            swapLanguages = new JButton("Swap");
            swapLanguages.setIcon(new FlatMenuArrowIcon());
            translateFromField = new JTextArea();

            //----------
            translateToPreferencePanel = new JPanel();
            translateToUserLanguagesPanel = new JPanel();//--------
            translateToPreferencePanelGL = new GridLayout(1, 3);
            translateToUserLanguages = new LinkedHashMap<>();
            translateToLanguageDropdown = new JComboBox();
            translateToField = new JTextArea("");
        }

        { // Configure Language Buttons
            loadLanguagesButtons();
        }

        // TranslateFromPreferencePanel Handling
        {
            translateFromUserLanguagesPanel.setLayout(translateFromPreferencePanelGL);
            for (var i : translateFromUserLanguages.entrySet())// add checkboxes to the panel
                translateFromUserLanguagesPanel.add(i.getValue());

            // TranslateFromPreferencePanel Handling
            translateFromPreferencePanel.add(translateFromUserLanguagesPanel);
            translateFromPreferencePanel.add(translateFromLanguageDropdown);
            translateFromPreferencePanel.add(swapLanguages);
        }

        // TranslateFromPreferencePanel Handling
        {
            translateToUserLanguagesPanel.setLayout(translateToPreferencePanelGL);
            for (var i : translateToUserLanguages.entrySet())// add checkboxes to the panel
                translateToUserLanguagesPanel.add(i.getValue());

            // TranslateFromPreferencePanel Handling
            translateToPreferencePanel.add(translateToUserLanguagesPanel);
            translateToPreferencePanel.add(translateToLanguageDropdown);
        }

        // TODO: Get languages by Google if there's any internet connection.(Keep the existing database though!)
        LoadSupportedLanguages();// We have a small database that contains a Language map.

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

        AddListeners();// Add listeners for the controls that exist in this function

        // Final touches
        {
            mainPanel.setLayout(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            mainPanel.add(translateFromPreferencePanel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            mainPanel.add(translateToPreferencePanel, gridBagConstraints);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.ipady = 100;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            mainPanel.add(translateFromField, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            mainPanel.add(translateToField, gridBagConstraints);

            mainFrame.setLayout(new GridLayout(1, 1));
            mainFrame.add(mainPanel);
            mainFrame.setAlwaysOnTop(false);
            mainFrame.pack();
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setResizable(false);
        }

        // TODO: Add tray functionality
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
    // info: ------------------------------------

    private void LoadSupportedLanguages() {
        for (var i : TranslationCore.getInstance().getAvailableLanguages().entrySet()) {
            translateFromLanguageDropdown.addItem(i.getKey());
            translateToLanguageDropdown.addItem(i.getKey());
        }
    }

    private void AddListeners() {
        // On TranslateField Change
        translateFromField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

                translatorThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //translatorThread.wait(500);

                            //final String from = translateFromLanguageDropdown.getItemAt(translateFromLanguageDropdown.getSelectedIndex()).toString();
                            //final String to = translateToLanguageDropdown.getItemAt(translateToLanguageDropdown.getSelectedIndex()).toString();

                            final String from = getSelectedLanguage(TranslationSides.TS_LEFT);
                            final String to = getSelectedLanguage(TranslationSides.TS_RIGHT);

                            System.out.println(from);
                            System.out.println(to);

                            final String translatedText =
                                    TranslationCore.getInstance().translate(TranslationCore.getInstance().getLanguageCodeByName(from), TranslationCore.getInstance().getLanguageCodeByName(to), translateFromField.getText());

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

        // On any translateFromUserLanguages button pressed
        for (Map.Entry<String, JToggleButton> currentEntry : translateFromUserLanguages.entrySet()) {
            currentEntry.getValue().addActionListener(e->{
                setSelectedLanguage(currentEntry.getKey(),TranslationSides.TS_LEFT);
            });
        }

        //translateToUserLanguages
        for (Map.Entry<String, JToggleButton> currentEntry : translateToUserLanguages.entrySet()) {
            currentEntry.getValue().addActionListener(e->{
                setSelectedLanguage(currentEntry.getKey(), TranslationSides.TS_RIGHT);
            });
        }

        // Swap button listener
        swapLanguages.addActionListener(e->{
            var leftSideButton = translateFromUserLanguages.get(getSelectedLanguage(TranslationSides.TS_LEFT));
            var rightSideButton = translateToUserLanguages.get(getSelectedLanguage(TranslationSides.TS_RIGHT));
            final String leftSideText = leftSideButton.getText();
            final String rightSideText = rightSideButton.getText();
            leftSideButton.setText(rightSideText);
            rightSideButton.setText(leftSideText);

            final Map<String,JToggleButton> temporary1 = translateFromUserLanguages;
            final Map<String,JToggleButton> temporary2 = translateToUserLanguages;
            translateFromUserLanguages.clear();
            translateToUserLanguages.clear();
            for(Map.Entry<String,JToggleButton> temp : temporary1.entrySet()){
                translateFromUserLanguages.put(temp.getValue().getText().toString(),temp.getValue());
            }
            for(Map.Entry<String,JToggleButton> temp1 : temporary2.entrySet()){
                translateToUserLanguages.put(temp1.getValue().getText().toString(),temp1.getValue());
            }
            //-----------------------
            final String rightSideTranslation = translateToField.getText();
            translateFromField.setText(rightSideTranslation);
        });


    }

    enum TranslationSides {
        TS_LEFT, TS_RIGHT
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
package gui.core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.icons.FlatDescendingSortIcon;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public final class GUICore {
    // Constants
    final static short translateFieldMaxLength = 500;
    private static GUICore single_instance = null;
    // info: ------------------------------------
    // Main GUI Elements
    private JFrame mainFrame = null;
    private List<JToggleButton> translateFromUserLanguages = null;
    private JButton swapLanguages = null;
    private JTextArea translateFromField = null;
    private List<JToggleButton> translateToUserLanguages = null;
    private JTextArea translateToField = null;
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
    public void UpdateTranslateFromField(String textToTranslate) {
        if (textToTranslate.length() >= translateFieldMaxLength) {
            JOptionPane.showMessageDialog(null, "Given text cannot be greater than 500 characters long!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        translateFromField.setText(textToTranslate);
    }

    private void loadLanguagesButtons() {
        boolean saveFound = false;
        if (!saveFound) { // Default initialization values
            // left side
            translateFromUserLanguages.add(new JToggleButton("English"));
            translateFromUserLanguages.add(new JToggleButton("Russian"));
            translateFromUserLanguages.add(new JToggleButton("French"));//-------
            setSelectedLanguage("English", TranslationSides.TS_LEFT);
            // right side
            translateToUserLanguages.add(new JToggleButton("Turkish"));
            translateToUserLanguages.add(new JToggleButton("Spanish"));
            translateToUserLanguages.add(new JToggleButton("Japanese"));//-------
            setSelectedLanguage("Turkish", TranslationSides.TS_RIGHT);
        }

        // TODO: Serialize user info to load on startup.
    }

    private List<String> getExistingLanguages(TranslationSides side) {
        List<String> out = new ArrayList<>();
        for (var i : getLanguageButtonsBySide(side))
        {
            out.add(i.getText());
        }
        return out;
    }

    private List<JToggleButton> getLanguageButtonsBySide(TranslationSides side) {
        if (side.equals(TranslationSides.TS_LEFT)) return translateFromUserLanguages;
        else return translateToUserLanguages;
    }

    // Returns the selected language button's value; Returns null it could not find any selected button
    private String getSelectedLanguage(TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side))
            if (i.isSelected()) return i.getText();
        // TODO: IDK WHETHER RETURNING THE BUTTON'S TEXT IS THE RIGHT WAY
        //  BECAUSE BUTTON'S TEXT CANNOT REPRESENT LANGUAGE NAME ALWAYS.
        //  BUT IT WORKS FOR NOW, SO I KEEP IT
        return null;
    }

    // Returns the selected language button's instance; Returns null it could not find any selected button
    private JToggleButton getSelectedLanguageButton(TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side))
            if (i.isSelected()) return i;
        return null;
    }

    // Attempts to select the given language.
    private void setSelectedLanguage(String lang, TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side))
            if(!i.getText().equals(lang))
                i.setSelected(false);
            else
                i.setSelected(true);// This will be called only once(if there's no bugs)
    }

    private boolean containsLanguage(String lang, TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side))
            if (i.getText().equals(lang)) return true;
        return false;
    }

    private void addLanguage(String lang, TranslationSides side) {
        if (!containsLanguage(lang, side)) {
            // We are going to insert a new language
            // Since we always will have 3 languages -> EN, RU, AZ
            // We can push the first and remove the last -> FR, EN, RU
            List<String> _languages_ = getExistingLanguages(side);
            _languages_.add(0,lang);
            _languages_.remove((_languages_.size()-1) < 0 ? 0 : _languages_.size()-1);
            //---------------------------------------------------------

            assert getLanguageButtonsBySide(side).size() == _languages_.size() : "EC:001-> getLanguageButtonsBySide(side).size() != _languages_.size()";

            // Assign values
            for(int c = 0; c < _languages_.size(); c++){
                getLanguageButtonsBySide(side).get(c).setText(_languages_.get(c));
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
            e.printStackTrace();
        }

        // Init UI Elements
        JPanel mainPanel;
        {
            mainFrame = new JFrame();
            mainPanel = new JPanel();
        }

        // Translate From Initialization
        // INFO: Translate Section
        JPanel translateFromPreferencePanel;
        GridLayout translateFromPreferencePanelGL;
        JPanel translateFromUserLanguagesPanel;
        JDropdownButton translateFromLanguageDropdown;
        // INFO: TranslateTo Section
        JPanel translateToPreferencePanel;
        GridLayout translateToPreferencePanelGL;
        JPanel translateToUserLanguagesPanel;
        JDropdownButton translateToLanguageDropdown;
        {
            translateFromPreferencePanel = new JPanel();
            translateFromUserLanguagesPanel = new JPanel();//--------
            translateFromPreferencePanelGL = new GridLayout(1, 3);
            translateFromUserLanguages = new ArrayList<>();
            translateFromLanguageDropdown = new JDropdownButton("", new FlatDescendingSortIcon(),
                    new ArrayList<>(TranslationCore.getInstance().getAvailableLanguages().keySet()));
            swapLanguages = new JButton("Swap");
            swapLanguages.setIcon(new FlatMenuArrowIcon());
            translateFromField = new JTextArea();

            //----------
            translateToPreferencePanel = new JPanel();
            translateToUserLanguagesPanel = new JPanel();//--------
            translateToPreferencePanelGL = new GridLayout(1, 3);
            translateToUserLanguages = new ArrayList<>();
            translateToLanguageDropdown = new JDropdownButton("", new FlatDescendingSortIcon(),
                    new ArrayList<>(TranslationCore.getInstance().getAvailableLanguages().keySet()));
            translateToField = new JTextArea("");
        }

        { // Configure Language Buttons
            loadLanguagesButtons();
        }

        // TranslateFromPreferencePanel Handling
        {
            translateFromUserLanguagesPanel.setLayout(translateFromPreferencePanelGL);
            for (var i : translateFromUserLanguages)// add buttons to the panel
                translateFromUserLanguagesPanel.add(i);

            // TranslateFromPreferencePanel Handling
            translateFromPreferencePanel.add(translateFromUserLanguagesPanel);
            translateFromPreferencePanel.add(swapLanguages);
            translateFromPreferencePanel.add(translateFromLanguageDropdown);
        }

        // TranslateFromPreferencePanel Handling
        {
            translateToUserLanguagesPanel.setLayout(translateToPreferencePanelGL);
            for (var i : translateToUserLanguages)// add buttons to the panel
                translateToUserLanguagesPanel.add(i);

            // TranslateFromPreferencePanel Handling
            translateToPreferencePanel.add(translateToUserLanguagesPanel);
            translateToPreferencePanel.add(translateToLanguageDropdown);
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
            gridBagConstraints.fill = GridBagConstraints.BOTH;
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

    private void AddListeners() {
        // On TranslateField Change
        translateFromField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                translatorThread = new Thread(() -> {
                    try {

                        final String from = getSelectedLanguage(TranslationSides.TS_LEFT);
                        final String to = getSelectedLanguage(TranslationSides.TS_RIGHT);

                        System.out.println(from);
                        System.out.println(to);

                        final String translatedText =
                                TranslationCore.getInstance().translate(TranslationCore.getInstance().getLanguageCodeByName(from), TranslationCore.getInstance().getLanguageCodeByName(to), translateFromField.getText());

                        translateToField.setText(translatedText);
                    } catch (IOException exc) {
                        exc.printStackTrace();
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
        for (var currentEntry : translateFromUserLanguages) {
            currentEntry.addActionListener(e -> RequestToSelectLanguage(currentEntry.getText(), TranslationSides.TS_LEFT));
        }

        //translateToUserLanguages
        for (var currentEntry : translateToUserLanguages) {
            currentEntry.addActionListener(e ->RequestToSelectLanguage(currentEntry.getText(), TranslationSides.TS_RIGHT));
        }

        // Swap button listener
        swapLanguages.addActionListener(e -> {
            final String leftSideText = getSelectedLanguageButton(TranslationSides.TS_LEFT).getText();
            final String rightSideText = getSelectedLanguageButton(TranslationSides.TS_RIGHT).getText();
            getSelectedLanguageButton(TranslationSides.TS_LEFT).setText(rightSideText);
            getSelectedLanguageButton(TranslationSides.TS_RIGHT).setText(leftSideText);

            //-----------------------
            final String rightSideTranslation = translateToField.getText();
            translateFromField.setText(rightSideTranslation);
        });


    }

    enum TranslationSides {
        TS_LEFT, TS_RIGHT
    }

    // Nested Classes
    private static final class JDropdownButton extends JButton {

        List<JMenuItem> menuItems = new ArrayList<>();
        List<String> items;
        JPopupMenu popupMenu = null;

        public JDropdownButton(String label, Icon icon, List<String> items) {
            super(label, icon);

            this.items = items;

            super.addActionListener(e -> onPopup() );
        }

        private void onPopup() {
            popupMenu = new JPopupMenu();

            items.clear();

            for (var i : items) {
                menuItems.add(new JMenuItem(i));
            }
            for (var i : menuItems)
                popupMenu.add(i);

            popupMenu.setVisible(true);

        }
    }

    private static final class JTextFieldLimit extends PlainDocument {
        private final int limit;

        JTextFieldLimit(int limit) {
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
package gui.core;

import aws.api.DictionaryAPI;
import aws.api.TextToSpeechAPI;
import aws.api.TranslateAPI;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.icons.*;
import gui.utils.ApplicationIcons;
import gui.utils.IconManager;
import utils.ClipboardManager;
import utils.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GUICore {

    // GUI Core
    TrayIcon trayIcon = null;
    private TextToSpeechAPI textToSpeechAPI = null;
    private DictionaryAPI dictionaryAPI = null;
    // Main GUI Elements
    final static short translateFieldMaxLength = 5000;
    private static GUICore single_instance = null;
    private JProgressBar mainProgressBar = null;
    private JDropdownButton translateFromLanguageDropdown;
    private JDropdownButton translateToLanguageDropdown;

    private JFrame mainFrame = null;
    private JPanel mainPanel = null;// To store all pages/panels inside a panel (for now)
    private List<JToggleButton> translateFromUserLanguages = null;
    private JButton translateFromReadLoud = null;
    private JButton translateFromCopyToClipboard = null;
    private JTextArea translateFromField = null;
    JScrollPane translateFromFieldScrollbar;
    private JProgressBar translateFromCharacterCount = null;
    private JButton swapLanguages = null;
    private List<JToggleButton> translateToUserLanguages = null;
    private JTextArea translateToField = null;
    JScrollPane translateToFieldScrollbar;
    private JButton translateToReadLoud = null;
    private JButton translateToCopyToClipboard = null;
    // Side-Thread(s)
    private Thread translatorThread = null;

    // Public Methods
    public static GUICore getInstance() {
        if (single_instance == null) single_instance = new GUICore();

        return single_instance;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    private void HandleProgressBar(boolean show, double seconds) {
        if (mainProgressBar == null) mainProgressBar = new JProgressBar();

        final long pastTime = System.currentTimeMillis();
        SwingUtilities.invokeLater(() -> {
            while (System.currentTimeMillis() < (pastTime + (seconds * 1000))) { //multiply by 1000 to get milliseconds
                final double passed = System.currentTimeMillis() - pastTime;
                final int percentage = (int) ((passed / (seconds * 1000)) * 100);
                mainProgressBar.setValue(percentage);
                //below code to update progress bar while running on thread
                mainProgressBar.update(mainProgressBar.getGraphics());
            }
            HandleProgressBar(false, 0);
        });

        if (show) {
            mainPanel.add(mainProgressBar);
        } else {
            mainPanel.remove(mainProgressBar);
        }

        getMainFrame().pack();
    }

    // INFO: RequestTranslate will do some pre-checks and set the translation area text if everything's okay.
    //  As the translation area text has a listener, it'll do translation job itself, we don't need to call anything else.
    public void UpdateTranslateFromField(String textToTranslate) {
        if (textToTranslate.length() >= translateFieldMaxLength) {
            JOptionPane.showMessageDialog(null, String.format("Given text cannot be greater than %d characters long!", translateFieldMaxLength), "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        translateFromField.setText(textToTranslate);
        translateFromField.requestFocus();
    }

    public void UpdateTranslateFromField() {
        UpdateTranslateFromField(translateFromField.getText());
    }

    private void loadLanguagesButtons() {
        boolean saveFound = false;
        if (!saveFound) { // Default initialization values
            // left side
            translateFromUserLanguages.add(new JToggleButton("English"));
            translateFromUserLanguages.add(new JToggleButton("Russian"));
            translateFromUserLanguages.add(new JToggleButton("French"));//-------
            // right side
            translateToUserLanguages.add(new JToggleButton("Turkish"));
            translateToUserLanguages.add(new JToggleButton("Spanish"));
            translateToUserLanguages.add(new JToggleButton("Japanese"));//-------

            // Set Selected Languages
            setSelectedLanguage("English", TranslationSides.TS_LEFT);
            setSelectedLanguage("Turkish", TranslationSides.TS_RIGHT);
        }

        // TODO: Serialize user info to load on startup.
    }

    private List<String> getExistingLanguages(TranslationSides side) {
        List<String> out = new ArrayList<>();
        for (var i : getLanguageButtonsBySide(side)) {
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
        return "";
    }

    private int getSelectedLanguageIndex(TranslationSides side) {
        for (int c = 0; c < getLanguageButtonsBySide(side).size(); c++) {
            var i = getLanguageButtonsBySide(side).get(c);
            if (i.isSelected()) return c;
        }
        return -1;
    }

    // Returns the selected language button's instance; Returns null it could not find any selected button
    private JToggleButton getSelectedLanguageButton(TranslationSides side) {
        for (var i : getLanguageButtonsBySide(side))
            if (i.isSelected()) return i;
        return null;
    }

    private void selectNextLanguage(TranslationSides side) {
        int currentSelectedIndex = getSelectedLanguageIndex(side)+1;

        if(currentSelectedIndex >= getExistingLanguages(side).size())
            currentSelectedIndex = 0;

        setSelectedLanguage(getExistingLanguages(side).get(currentSelectedIndex),side);
    }

    // Returns the opposite side of the given side -> EX: RIGHT for LEFT, LEFT for RIGHT
    private TranslationSides getOppositeSide(TranslationSides side){
        return side.equals(TranslationSides.TS_LEFT) ? TranslationSides.TS_RIGHT : TranslationSides.TS_LEFT;
    }

    // Attempts to select the given language.
    private void setSelectedLanguage(String lang, TranslationSides side) {
            // This will be called only once(if there's no bugs)
            for (var i : getLanguageButtonsBySide(side))
                i.setSelected(i.getText().equals(lang));

                // be sure that we don't try to translate to same language
        if (getSelectedLanguage(getOppositeSide(side)).equals(lang))
            selectNextLanguage(getOppositeSide(side));

        UpdateTranslateFromField();// Update translation in any case
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
            _languages_.add(0, lang);
            _languages_.remove((_languages_.size() - 1) <= 0 ? 0 : _languages_.size() - 1);
            //---------------------------------------------------------

            assert getLanguageButtonsBySide(side).size() == _languages_.size() : "EC:001-> getLanguageButtonsBySide(side).size() != _languages_.size()";

            // Assign values
            for (int c = 0; c < _languages_.size(); c++) {
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

        // Init other elements
        textToSpeechAPI = new TextToSpeechAPI();
        textToSpeechAPI.Initialize();// TODO: Load recent voice and output format (serialized ones)

        dictionaryAPI = new DictionaryAPI();

        // Init UI Elements
        JPanel translatorPanel;
        {
            mainFrame = new JFrame(Constants.APP_NAME);
            translatorPanel = new JPanel();
        }

        JMenuBar mainMenuBar;
        JMenu mainMenu, mainSubmenu;
        JMenuItem mainMenuItemTheme;
        JMenuItem mainMenuItemOptions;
        JMenuItem mainMenuItemClose;
        JMenuItem themeDark;
        JMenuItem themeLight;

        // Translate From Initialization
        // INFO: Translate Section
        JPanel translateSectionHeader;
        JPanel translateFromAdditionalPanel;
        // INFO: TranslateTo Section
        JPanel translateToAdditionalPanel;
        {
            mainPanel = new JPanel();

            mainMenuBar = new JMenuBar();
            mainMenu = new JMenu("Menu");
            mainSubmenu = new JMenu("Submenu");
            mainMenuItemTheme = new JMenuItem("Theme");
            themeDark = new JMenuItem("Dark");
            themeLight = new JMenuItem("light");
            mainMenuItemOptions = new JMenuItem("Options");
            mainMenuItemClose = new JMenuItem("Close");

            translateSectionHeader = new JPanel();

            translateFromUserLanguages = new ArrayList<>();
            translateFromLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(), new ArrayList<>(TranslateAPI.getInstance().getAvailableLanguages().keySet()));
            swapLanguages = new JButton("Swap", new FlatTreeLeafIcon());
            translateFromField = new JTextArea();
            translateFromFieldScrollbar = new JScrollPane(translateFromField);
            translateFromAdditionalPanel = new JPanel();
            translateFromReadLoud = new JButton("Play", new FlatMenuArrowIcon());
            translateFromCopyToClipboard = new JButton("Copy to Clipboard", new FlatFileViewFileIcon());
            translateFromCharacterCount = new JProgressBar(0,translateFieldMaxLength);
            //----------
            translateToUserLanguages = new ArrayList<>();
            translateToLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(), new ArrayList<>(TranslateAPI.getInstance().getAvailableLanguages().keySet()));
            translateToField = new JTextArea("");
            translateToAdditionalPanel = new JPanel();
            translateToFieldScrollbar = new JScrollPane(translateToField);
            translateToReadLoud = new JButton("Play", new FlatMenuArrowIcon());
            translateToCopyToClipboard = new JButton("Copy to Clipboard", new FlatFileViewFileIcon());
        }

        { // Configure main menu
            mainMenuBar.add(mainMenu);
            mainMenu.setIcon(new FlatTreeOpenIcon());

            ImageIcon t = IconManager.getInstance().getIcon(ApplicationIcons.ICON_MENU_THEME);
            mainMenuItemTheme.setIcon(t);
            mainMenu.add(mainMenuItemTheme);

            mainSubmenu.add(themeDark);
            //themeDark.setIcon();

            mainSubmenu.add(themeLight);
            //themeLight.setIcon();

            mainMenu.add(mainMenuItemOptions);
            //mainMenuItemOptions.setIcon();

            mainMenu.add(mainMenuItemClose);
            //mainMenuItemClose.setIcon();
        }

        { // Configure Language Buttons
            loadLanguagesButtons();
        }

        // TranslateFromPreferencePanel Handling
        {
            translateSectionHeader.setLayout(new FlowLayout());
            for (var i : translateFromUserLanguages)
                translateSectionHeader.add(i);

            // TranslateFromPreferencePanel
            translateSectionHeader.add(translateFromLanguageDropdown);
            translateSectionHeader.add(swapLanguages);
            for (var i : translateToUserLanguages)
                translateSectionHeader.add(i);
            translateSectionHeader.add(translateToLanguageDropdown);
        }

        // Handle Translate Field Configuration
        {
            translateFromField.setLineWrap(true);
            translateFromField.setWrapStyleWord(true);
            translateFromField.setDocument(new JTextFieldLimit(translateFieldMaxLength));
            translateFromFieldScrollbar.setPreferredSize(new Dimension(translateFromFieldScrollbar.getWidth(), 150));
            //---------
            translateToField.setLineWrap(true);
            translateToField.setWrapStyleWord(true);
            translateToField.setEditable(false);
            translateToFieldScrollbar.setPreferredSize(new Dimension(translateToFieldScrollbar.getWidth(), 150));
        }

        // Character count Handling
        {
            translateFromCharacterCount.setStringPainted(true);
            translateFromCharacterCount.setString(String.format("%d / %d", translateFromField.getText().length(), translateFieldMaxLength));
        }

        AddEventListeners();// Add listeners for the controls that exist in this function

        // Final touches
        {
            translatorPanel.setLayout(new BorderLayout());

            translatorPanel.add(translateSectionHeader, BorderLayout.PAGE_START);

            JPanel translateFields = new JPanel();
            translateFields.setLayout(new GridLayout(1,2));
            translateFields.add(translateFromFieldScrollbar);
            translateFields.add(translateToFieldScrollbar);

            translatorPanel.add(translateFields, BorderLayout.CENTER);

            // translateFrom Additional Panel
            translateFromAdditionalPanel.setLayout(new GridLayout());
            translateFromAdditionalPanel.add(translateFromReadLoud);
            translateFromAdditionalPanel.add(translateFromCopyToClipboard);
            translateFromAdditionalPanel.add(translateFromCharacterCount);
            // translateTo Additional Panel
            translateToAdditionalPanel.setLayout(new GridLayout());
            translateToAdditionalPanel.add(translateToReadLoud);
            translateToAdditionalPanel.add(translateToCopyToClipboard);

            JPanel translateFieldsFooterPanel = new JPanel();
            translateFieldsFooterPanel.setLayout(new GridLayout(1,2));

            translateFieldsFooterPanel.add(translateFromAdditionalPanel);
            translateFieldsFooterPanel.add(translateToAdditionalPanel);

            translatorPanel.add(translateFieldsFooterPanel, BorderLayout.PAGE_END);

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(translatorPanel);

            mainFrame.setLayout(new BorderLayout());
            mainFrame.add(mainMenu, BorderLayout.PAGE_START);
            mainFrame.add(mainPanel, BorderLayout.CENTER);
            mainFrame.setAlwaysOnTop(false);
            mainFrame.pack();
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setResizable(false);
        }

        SetIcon();
        AddTrayIcon();
    }

    private void SetIcon() {
        File file = new File(Constants.APP_ICON_PATH);

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(mainFrame, "App icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedImage bImage = ImageIO.read(file);
            getMainFrame().setIconImage(bImage);

            //set icon on system tray, as in Mac OS X system
            if (System.getProperty("os.name").contains("Mac")) {
                final Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(bImage);// - OrkhanGG TODO: test it on Mac
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void AddTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(mainFrame, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(Constants.APP_TRAY_ICON_PATH);
        final PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(image, Constants.APP_NAME, popup);
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(1));
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    // info: ------------------------------------

    private void AddEventListeners() {

        translateFromCharacterCount.addChangeListener(e->{
            translateFromCharacterCount.setString(String.format("%d / %d", translateFromField.getText().length(), translateFieldMaxLength));
            translateFromCharacterCount.update(translateFromCharacterCount.getGraphics());
        });

        // On TranslateField Change(When user stops typing)
        translateFromField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                translateFromCharacterCount.setValue(translateFromField.getText().length());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                translateFromCharacterCount.setValue(translateFromField.getText().length());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        DeferredDocumentListener listener = new DeferredDocumentListener(500, e -> {
            translatorThread = new Thread(() -> {// TODO: this method is called periodically. Optimization is required!
                try {
                    final String from = getSelectedLanguage(TranslationSides.TS_LEFT);
                    final String to = getSelectedLanguage(TranslationSides.TS_RIGHT);

                    final String translatedText = TranslateAPI.getInstance().translate(TranslateAPI.getInstance().getLanguageCodeByName(from), TranslateAPI.getInstance().getLanguageCodeByName(to), translateFromField.getText());

                    // TODO: Create a method for this:
                    translateToField.setText(translatedText);

                    translateToFieldScrollbar.getVerticalScrollBar().setValue(translateToFieldScrollbar.getVerticalScrollBar().getMaximum());
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            });
            translatorThread.start();
        }, false);
        translateFromField.getDocument().addDocumentListener(listener);
        translateFromField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                listener.start();
            }

            @Override
            public void focusLost(FocusEvent e) {
                listener.stop();
            }
        });

        // On any translateFromUserLanguages button pressed
        for (var currentEntry : translateFromUserLanguages) {
            currentEntry.addActionListener(e -> RequestToSelectLanguage(currentEntry.getText(), TranslationSides.TS_LEFT));
        }

        //translateToUserLanguages
        for (var currentEntry : translateToUserLanguages) {
            currentEntry.addActionListener(e -> RequestToSelectLanguage(currentEntry.getText(), TranslationSides.TS_RIGHT));
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

        // Dropdown events
        if (translateFromLanguageDropdown.getMenuItems().size() > 0 && translateToLanguageDropdown.getMenuItems().size() > 0) {
            for (var i : translateFromLanguageDropdown.getMenuItems()) {
                i.addActionListener(e -> RequestToSelectLanguage(i.getText(), TranslationSides.TS_LEFT));
            }
            for (var i : translateToLanguageDropdown.getMenuItems()) {
                i.addActionListener(e -> RequestToSelectLanguage(i.getText(), TranslationSides.TS_RIGHT));
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Either translate-From/To LanguageDropdown button's menu items don't exist! ", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Text to Speech events
        // TODO: Depending on TTS support over the languages, these buttons will be enabled/disabled!
        translateFromReadLoud.addActionListener(e->
        {
            Thread textToSpeechThread = new Thread(() -> {
                textToSpeechAPI.RequestSetStream(translateFromField.getText());
                textToSpeechAPI.RequestPlayStream();
            });
            textToSpeechThread.start();
        });
        translateToReadLoud.addActionListener(e-> {
            textToSpeechAPI.RequestSetStream(translateToField.getText());
            textToSpeechAPI.RequestPlayStream();
        });

        // Copy to Clipboard events
        translateFromCopyToClipboard.addActionListener(e->{
            try {
                ClipboardManager.getInstance().setClipboardText(translateFromField.getText());
                String originalText = translateFromCopyToClipboard.getText();
                translateFromCopyToClipboard.setText("Copied!");
                trayIcon.displayMessage(Constants.APP_NAME,"Copied:"+translateFromField.getText(), TrayIcon.MessageType.INFO);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                translateFromCopyToClipboard.setText(originalText);
                            }
                        },
                        1000
                );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        translateToCopyToClipboard.addActionListener(e->{
            try {
                ClipboardManager.getInstance().setClipboardText(translateToField.getText());
                String originalText = translateToCopyToClipboard.getText();
                translateToCopyToClipboard.setText("Copied!");
                trayIcon.displayMessage(Constants.APP_NAME,"Copied:"+translateToField.getText(), TrayIcon.MessageType.INFO);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                translateToCopyToClipboard.setText(originalText);
                            }
                        },
                        1000
                );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    enum TranslationSides {
        TS_LEFT, TS_RIGHT
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

    // Nested Classes
    private final class JDropdownButton extends JButton {
        private final List<JMenuItem> menuItems;
        private JPopupMenu popupMenu = null;

        public JDropdownButton(String label, Icon icon, List<String> items) {

            super(label, icon);

            menuItems = new ArrayList<>();
            for (var i : items) {
                menuItems.add(new JMenuItem(i));
            }

            super.addActionListener(e -> {
                popupMenu = new JPopupMenu();

                for (var i : menuItems)
                    popupMenu.add(i);

                popupMenu.show(this, 10, 10);
            });
        }

        public List<JMenuItem> getMenuItems() {
            return menuItems;
        }

    }

    public class DeferredDocumentListener implements DocumentListener {

        private final Timer timer;

        public DeferredDocumentListener(int timeOut, ActionListener listener, boolean repeats) {
            timer = new Timer(timeOut, listener);
            timer.setRepeats(repeats);
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }

    }
}
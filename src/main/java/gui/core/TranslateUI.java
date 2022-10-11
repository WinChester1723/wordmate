package gui.core;

import aws.api.DictionaryAPI;
import aws.api.TextToSpeechAPI;
import aws.api.TranslateAPI;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.icons.FlatDescendingSortIcon;
import com.formdev.flatlaf.icons.FlatFileViewFileIcon;
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

public final class TranslateUI {
    private static TranslateUI single_instance = null;
    BufferedImage TranslateUIIcon = null;
    TrayIcon translateUITrayIcon = null;

    // API Objects
    private TextToSpeechAPI textToSpeechAPI = null;
    private DictionaryAPI dictionaryAPI = null;

    // Main GUI Elements
    final static short translateFieldMaxLength = 5000;
    private JProgressBar mainProgressBar = null;
    private JDropdownButton inputLanguageDropdown;
    private JDropdownButton outputLanguageDropdown;

    private JFrame translateUIFrame = null;
    private JPanel translateUIPanel = null;

    // GUI Controls
    private List<JToggleButton> inputUserLanguages = null;
    private JButton inputReadLoudButton = null;
    private JButton inputCopyToClipboard = null;
    private JTextArea inputField = null;
    JScrollPane inputFieldScrollbar;
    private JProgressBar inputFieldCharacterCountIndicator = null;
    private JButton swapLanguages = null;
    private List<JToggleButton> outputUserLanguages = null;
    private JTextArea outputField = null;
    JScrollPane outputFieldScrollbar = null;
    private JButton outputReadLoud = null;
    private JButton outputCopyToClipboard = null;

    // Methods
    public static TranslateUI getInstance() {
        if (single_instance == null) single_instance = new TranslateUI();

        return single_instance;
    }
    public JFrame getTranslateUIFrame() {
        return translateUIFrame;
    }
    public void Initialize() throws BadLocationException {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Init other elements
        dictionaryAPI = new DictionaryAPI();

        // Init UI Elements
        JPanel translatorPanel;
        {
            translateUIFrame = new JFrame(Constants.APP_NAME);
            translatorPanel = new JPanel();
        }

        JMenuBar translateUIMenuBar;
        JMenu translateUISettingsMenu, translateUIPreferencesMenu;
        JPanel ioHeaderPanel;
        JPanel inputFooterPanel;
        JPanel outputFooterPanel;
        JPanel ioFields;
        JPanel ioFieldsFooterPanel;

        {
            translateUIMenuBar = new JMenuBar();
            translateUISettingsMenu = new JMenu("Settings");
            translateUIPreferencesMenu = new JMenu("Preferences");
            translateUIPanel = new JPanel();
            ioHeaderPanel = new JPanel();
            inputUserLanguages = new ArrayList<>();
            inputLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(), new ArrayList<>(TranslateAPI.getInstance().getAvailableLanguages().keySet()));
            swapLanguages = new JButton("Swap", IconManager.getInstance().getIcon(ApplicationIcons.ICON_SWAP));
            inputField = new JTextArea();
            inputFieldScrollbar = new JScrollPane(inputField);
            inputFooterPanel = new JPanel();
            inputReadLoudButton = new JButton("Play",  IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));
            inputCopyToClipboard = new JButton("Copy to Clipboard", new FlatFileViewFileIcon());
            inputFieldCharacterCountIndicator = new JProgressBar(0,translateFieldMaxLength);
            outputUserLanguages = new ArrayList<>();
            outputLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(), new ArrayList<>(TranslateAPI.getInstance().getAvailableLanguages().keySet()));
            outputField = new JTextArea("");
            outputFooterPanel = new JPanel();
            outputFieldScrollbar = new JScrollPane(outputField);
            outputReadLoud = new JButton("Play", IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));
            outputCopyToClipboard = new JButton("Copy to Clipboard", new FlatFileViewFileIcon());
            ioFields = new JPanel();
            ioFieldsFooterPanel = new JPanel();
        }

        ImageIcon settingsIcon = IconManager.getInstance().getIcon(ApplicationIcons.ICON_MENU_OPTIONS);
        translateUISettingsMenu.setIcon(settingsIcon);
        translateUIMenuBar.add(translateUISettingsMenu);

        ImageIcon preferencesIcon = IconManager.getInstance().getIcon(ApplicationIcons.ICON_MENU_THEME);
        translateUIPreferencesMenu.setIcon(preferencesIcon);
        translateUIMenuBar.add(translateUIPreferencesMenu);

        { // Setup Language Buttons
            setupLanguagesButtons();
        }

        {   // Setup header
            ioHeaderPanel.setLayout(new FlowLayout());
            for (var i : inputUserLanguages)
                ioHeaderPanel.add(i);
            ioHeaderPanel.add(inputLanguageDropdown);
            ioHeaderPanel.add(swapLanguages);
            for (var i : outputUserLanguages)
                ioHeaderPanel.add(i);
            ioHeaderPanel.add(outputLanguageDropdown);
        }

        {   // Setup input/output fields
            inputField.setLineWrap(true);
            inputField.setWrapStyleWord(true);
            inputField.setDocument(new JTextFieldLimit(translateFieldMaxLength));
            inputFieldScrollbar.setPreferredSize(new Dimension(inputFieldScrollbar.getWidth(), 150));
            outputField.setLineWrap(true);
            outputField.setWrapStyleWord(true);
            outputField.setEditable(false);
            outputFieldScrollbar.setPreferredSize(new Dimension(outputFieldScrollbar.getWidth(), 150));
        }

        {   // Setup character count indicator
            inputFieldCharacterCountIndicator.setStringPainted(true);
            inputFieldCharacterCountIndicator.setString(String.format("%d / %d", inputField.getText().length(), translateFieldMaxLength));
        }

        {   // Setup layout
            translatorPanel.setLayout(new BorderLayout());
            translatorPanel.add(ioHeaderPanel, BorderLayout.PAGE_START);
            ioFields.setLayout(new GridLayout(1,2));
            ioFields.add(inputFieldScrollbar);
            ioFields.add(outputFieldScrollbar);
            translatorPanel.add(ioFields, BorderLayout.CENTER);

            inputFooterPanel.setLayout(new GridLayout());
            inputFooterPanel.add(inputReadLoudButton);
            inputFooterPanel.add(inputCopyToClipboard);
            inputFooterPanel.add(inputFieldCharacterCountIndicator);
            outputFooterPanel.setLayout(new GridLayout());
            outputFooterPanel.add(outputReadLoud);
            outputFooterPanel.add(outputCopyToClipboard);
            ioFieldsFooterPanel.setLayout(new GridLayout(1,2));
            ioFieldsFooterPanel.add(inputFooterPanel);
            ioFieldsFooterPanel.add(outputFooterPanel);
            translatorPanel.add(ioFieldsFooterPanel, BorderLayout.PAGE_END);

            translateUIPanel.setLayout(new BoxLayout(translateUIPanel, BoxLayout.Y_AXIS));
            translateUIPanel.add(translatorPanel);
            translateUIFrame.setJMenuBar(translateUIMenuBar);
            translateUIFrame.add(translateUIPanel, BorderLayout.CENTER);
            translateUIFrame.setAlwaysOnTop(false);
            translateUIFrame.pack();
            translateUIFrame.setVisible(true);
            translateUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            translateUIFrame.setResizable(false);
        }

        setIcon();
        addTrayIcon();
        addEventListeners();
    }
    public void updateInputField(String textToTranslate) {
        if (textToTranslate.length() >= translateFieldMaxLength) {
            JOptionPane.showMessageDialog(null, String.format("Given text cannot be greater than %d characters long!", translateFieldMaxLength), "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        inputField.setText(textToTranslate);
        inputField.requestFocus();
    }
    public void updateInputField() {
        updateInputField(inputField.getText());
    }
    private void setupLanguagesButtons() {
        boolean saveFound = false;
        if (!saveFound) { // Default initialization values
            // left side
            inputUserLanguages.add(new JToggleButton("English"));
            inputUserLanguages.add(new JToggleButton("Russian"));
            inputUserLanguages.add(new JToggleButton("French"));//-------
            // right side
            outputUserLanguages.add(new JToggleButton("Turkish"));
            outputUserLanguages.add(new JToggleButton("Spanish"));
            outputUserLanguages.add(new JToggleButton("Japanese"));//-------

            // Set Selected Languages
            headerSetSelectedLanguage("English", TranslationInputOutputEnum.TS_INPUT);
            headerSetSelectedLanguage("Turkish", TranslationInputOutputEnum.TS_OUTPUT);
        }

        // TODO: Serialize user info to load on startup.
    }
    private List<String> headerGetExistingLanguages(TranslationInputOutputEnum side) {
        List<String> out = new ArrayList<>();
        for (var i : headerGetLanguageButtonsBySide(side)) {
            out.add(i.getText());
        }
        return out;
    }
    private List<JToggleButton> headerGetLanguageButtonsBySide(TranslationInputOutputEnum side) {
        if (side.equals(TranslationInputOutputEnum.TS_INPUT)) return inputUserLanguages;
        else return outputUserLanguages;
    }
    private String headerGetSelectedLanguage(TranslationInputOutputEnum side) {
        for (var i : headerGetLanguageButtonsBySide(side))
            if (i.isSelected()) return i.getText();
        // TODO: IDK WHETHER RETURNING THE BUTTON'S TEXT IS THE RIGHT WAY
        //  BECAUSE BUTTON'S TEXT CANNOT REPRESENT LANGUAGE NAME ALWAYS.
        //  BUT IT WORKS FOR NOW, SO I KEEP IT
        return "";
    }
    private int headerGetSelectedLanguageIndex(TranslationInputOutputEnum side) {
        for (int c = 0; c < headerGetLanguageButtonsBySide(side).size(); c++) {
            var i = headerGetLanguageButtonsBySide(side).get(c);
            if (i.isSelected()) return c;
        }
        return -1;
    }
    private JToggleButton headerGetSelectedLanguageButton(TranslationInputOutputEnum side) {
        for (var i : headerGetLanguageButtonsBySide(side))
            if (i.isSelected()) return i;
        return null;
    }
    private void headerSelectNextLanguage(TranslationInputOutputEnum side) {
        int currentSelectedIndex = headerGetSelectedLanguageIndex(side)+1;

        if(currentSelectedIndex >= headerGetExistingLanguages(side).size())
            currentSelectedIndex = 0;

        headerSetSelectedLanguage(headerGetExistingLanguages(side).get(currentSelectedIndex),side);
    }
    private TranslationInputOutputEnum headerGetOppositeSide(TranslationInputOutputEnum side){
        return side.equals(TranslationInputOutputEnum.TS_INPUT) ? TranslationInputOutputEnum.TS_OUTPUT : TranslationInputOutputEnum.TS_INPUT;
    }
    private void headerSetSelectedLanguage(String lang, TranslationInputOutputEnum side) {
            // This will be called only once(if there's no bugs)
            for (var i : headerGetLanguageButtonsBySide(side))
                i.setSelected(i.getText().equals(lang));

                // be sure that we don't try to translate to same language
        if (headerGetSelectedLanguage(headerGetOppositeSide(side)).equals(lang))
            headerSelectNextLanguage(headerGetOppositeSide(side));

        updateInputField();// Update translation in any case
    }
    public void headerRequestToSelectLanguage(String lang, TranslationInputOutputEnum side) {
        if (headerContainsLanguage(lang, side)) {
            headerSetSelectedLanguage(lang, side);
        } else {
            headerAddLanguage(lang, side);
        }
    }
    private void headerAddLanguage(String lang, TranslationInputOutputEnum side) {
        if (!headerContainsLanguage(lang, side)) {
            // We are going to insert a new language
            // Since we always will have 3 languages -> EN, RU, AZ
            // We can push the first and remove the last -> FR, EN, RU
            List<String> _languages_ = headerGetExistingLanguages(side);
            _languages_.add(0, lang);
            _languages_.remove((_languages_.size() - 1) <= 0 ? 0 : _languages_.size() - 1);
            //---------------------------------------------------------

            assert headerGetLanguageButtonsBySide(side).size() == _languages_.size() : "EC:001-> headerGetLanguageButtonsBySide(side).size() != _languages_.size()";

            // Assign values
            for (int c = 0; c < _languages_.size(); c++) {
                headerGetLanguageButtonsBySide(side).get(c).setText(_languages_.get(c));
            }
        }
        // Finally, whether it's recently added or not, select the button
        headerSetSelectedLanguage(lang, side);
    }
    private boolean headerContainsLanguage(String lang, TranslationInputOutputEnum side) {
        for (var i : headerGetLanguageButtonsBySide(side))
            if (i.getText().equals(lang)) return true;
        return false;
    }
    private void addEventListeners() {

        inputFieldCharacterCountIndicator.addChangeListener(e->{
            inputFieldCharacterCountIndicator.setString(String.format("%d / %d", inputField.getText().length(), translateFieldMaxLength));
            inputFieldCharacterCountIndicator.update(inputFieldCharacterCountIndicator.getGraphics());
        });

        // On TranslateField Change(When user stops typing)
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                inputFieldCharacterCountIndicator.setValue(inputField.getText().length());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                inputFieldCharacterCountIndicator.setValue(inputField.getText().length());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        DeferredDocumentListener listener = new DeferredDocumentListener(500, e -> {
            Thread translatorThread = new Thread(() -> {// TODO: this method is called periodically. Optimization is required!
                try {
                    final String from = headerGetSelectedLanguage(TranslationInputOutputEnum.TS_INPUT);
                    final String to = headerGetSelectedLanguage(TranslationInputOutputEnum.TS_OUTPUT);

                    final String translatedText = TranslateAPI.getInstance().translate(TranslateAPI.getInstance().getLanguageCodeByName(from), TranslateAPI.getInstance().getLanguageCodeByName(to), inputField.getText());

                    // TODO: Create a method for this:
                    outputField.setText(translatedText);

                    outputFieldScrollbar.getVerticalScrollBar().setValue(outputFieldScrollbar.getVerticalScrollBar().getMaximum());
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            });
            translatorThread.start();
        }, false);
        inputField.getDocument().addDocumentListener(listener);
        inputField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                listener.start();
            }

            @Override
            public void focusLost(FocusEvent e) {
                listener.stop();
            }
        });

        // On any inputUserLanguages button pressed
        for (var currentEntry : inputUserLanguages) {
            currentEntry.addActionListener(e -> headerRequestToSelectLanguage(currentEntry.getText(), TranslationInputOutputEnum.TS_INPUT));
        }

        for (var currentEntry : outputUserLanguages) {
            currentEntry.addActionListener(e -> headerRequestToSelectLanguage(currentEntry.getText(), TranslationInputOutputEnum.TS_OUTPUT));
        }

        // Swap button listener
        swapLanguages.addActionListener(e -> {
            final String leftSideText = headerGetSelectedLanguageButton(TranslationInputOutputEnum.TS_INPUT).getText();
            final String rightSideText = headerGetSelectedLanguageButton(TranslationInputOutputEnum.TS_OUTPUT).getText();
            headerGetSelectedLanguageButton(TranslationInputOutputEnum.TS_INPUT).setText(rightSideText);
            headerGetSelectedLanguageButton(TranslationInputOutputEnum.TS_OUTPUT).setText(leftSideText);

            //-----------------------
            final String rightSideTranslation = outputField.getText();
            inputField.setText(rightSideTranslation);
        });

        // Dropdown events
        if (inputLanguageDropdown.getMenuItems().size() > 0 && outputLanguageDropdown.getMenuItems().size() > 0) {
            for (var i : inputLanguageDropdown.getMenuItems()) {
                i.addActionListener(e -> headerRequestToSelectLanguage(i.getText(), TranslationInputOutputEnum.TS_INPUT));
            }
            for (var i : outputLanguageDropdown.getMenuItems()) {
                i.addActionListener(e -> headerRequestToSelectLanguage(i.getText(), TranslationInputOutputEnum.TS_OUTPUT));
            }
        } else {
            JOptionPane.showMessageDialog(translateUIFrame, "Either translate-From/To LanguageDropdown button's menu items don't exist! ", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Text to Speech events
        // TODO: Depending on TTS support over the languages, these buttons will be enabled/disabled!
        inputReadLoudButton.addActionListener(e->
        {
            if(inputField.getText().isEmpty() || inputField.getText().isBlank())
                return;

            Thread textToSpeechThread = new Thread(() -> {

                // Reset icons first
                inputReadLoudButton.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));

                inputReadLoudButton.setEnabled(false);

                if(textToSpeechAPI != null )
                    if(textToSpeechAPI.isPlaying()) {
                        inputReadLoudButton.setEnabled(false);
                        textToSpeechAPI.RequestToStopStream();
                        inputReadLoudButton.setEnabled(true);
                        return;
                    }

                // Then set which needs to be changed
                inputReadLoudButton.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_STOP));

                textToSpeechAPI = new TextToSpeechAPI();
                textToSpeechAPI.Initialize();// TODO: Load recent voice and output format (serialized ones)

                textToSpeechAPI.addOnStopListener(()->{
                    // Reset icon on stop
                    inputReadLoudButton.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));
                });

                // Then TTS request
                textToSpeechAPI.RequestSetStream(inputField.getText());

                inputReadLoudButton.setEnabled(true);

                textToSpeechAPI.RequestPlayStream();
            });
            textToSpeechThread.start();
        });

        //--------------------------------------------

        outputReadLoud.addActionListener(e->
        {
            if(outputField.getText().isEmpty() || outputField.getText().isBlank())
                return;

            Thread textToSpeechThread = new Thread(() -> {

                // Reset icons first
                outputReadLoud.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));

                outputReadLoud.setEnabled(false);

                if(textToSpeechAPI != null )
                    if(textToSpeechAPI.isPlaying()) {
                        outputReadLoud.setEnabled(false);
                        textToSpeechAPI.RequestToStopStream();
                        outputReadLoud.setEnabled(true);
                        return;
                    }

                // Then set which needs to be changed
                outputReadLoud.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_STOP));

                textToSpeechAPI = new TextToSpeechAPI();
                textToSpeechAPI.Initialize();// TODO: Load recent voice and output format (serialized ones)

                textToSpeechAPI.addOnStopListener(()->{
                    // Reset icon on stop
                    outputReadLoud.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));
                });

                // Then TTS request
                textToSpeechAPI.RequestSetStream(outputField.getText());

                outputReadLoud.setEnabled(true);

                textToSpeechAPI.RequestPlayStream();
            });
            textToSpeechThread.start();
        });

        // Copy to Clipboard events
        inputCopyToClipboard.addActionListener(e->{
            try {
                ClipboardManager.getInstance().setClipboardText(inputField.getText());
                String originalText = inputCopyToClipboard.getText();
                inputCopyToClipboard.setText("Copied!");
                translateUITrayIcon.displayMessage(Constants.APP_NAME,"Copied:"+ inputField.getText(), TrayIcon.MessageType.INFO);

                inputCopyToClipboard.setEnabled(false);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                inputCopyToClipboard.setText(originalText);
                                inputCopyToClipboard.setEnabled(true);
                            }
                        },
                        1000
                );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        outputCopyToClipboard.addActionListener(e->{
            try {
                ClipboardManager.getInstance().setClipboardText(outputField.getText());
                String originalText = outputCopyToClipboard.getText();
                outputCopyToClipboard.setText("Copied!");
                translateUITrayIcon.displayMessage(Constants.APP_NAME,"Copied:"+ outputField.getText(), TrayIcon.MessageType.INFO);

                outputCopyToClipboard.setEnabled(false);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                outputCopyToClipboard.setText(originalText);
                                outputCopyToClipboard.setEnabled(true);
                            }
                        },
                        1000
                );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    private void addTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(translateUIFrame, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(Constants.APP_TRAY_ICON_PATH);
        final PopupMenu popup = new PopupMenu();
        translateUITrayIcon = new TrayIcon(image, Constants.APP_NAME, popup);
        translateUITrayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(1));
        popup.add(exitItem);

        translateUITrayIcon.setPopupMenu(popup);

        try {
            tray.add(translateUITrayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }
    private void setIcon() {
        File file = new File(Constants.APP_ICON_PATH);

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(translateUIFrame, "App icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            TranslateUIIcon = ImageIO.read(file);
            getTranslateUIFrame().setIconImage(TranslateUIIcon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    enum TranslationInputOutputEnum {
        TS_INPUT, TS_OUTPUT
    }
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
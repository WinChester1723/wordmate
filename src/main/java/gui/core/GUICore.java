package gui.core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.icons.FlatDescendingSortIcon;
import com.formdev.flatlaf.icons.FlatMenuArrowIcon;
import main.java.io.translator.TranslationCore;
import utils.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GUICore {
    // utils.Constants
    final static short translateFieldMaxLength = 500;
    private static GUICore single_instance = null;
    JProgressBar mainProgressBar = null;
    // info: ------------------------------------
    // Main GUI Elements
    private JFrame mainFrame = null;
    private JPanel mainPanel = null;// To store all pages/panels inside a panel (for now)
    private List<JToggleButton> translateFromUserLanguages = null;
    private JButton swapLanguages = null;
    private JTextArea translateFromField = null;
    private List<JToggleButton> translateToUserLanguages = null;
    private JTextArea translateToField = null;
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
        if (mainProgressBar == null)
            mainProgressBar = new JProgressBar();

        final long pastTime = System.currentTimeMillis();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                while (System.currentTimeMillis() < (pastTime + (seconds * 1000))) { //multiply by 1000 to get milliseconds
                    final double passed = System.currentTimeMillis() - pastTime;
                    final int percentage = (int) ((passed / (seconds * 1000)) * 100);
                    mainProgressBar.setValue(percentage);
                    //below code to update progress bar while running on thread
                    mainProgressBar.update(mainProgressBar.getGraphics());
                }
                HandleProgressBar(false, 0);
            }
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
            JOptionPane.showMessageDialog(null, "Given text cannot be greater than 500 characters long!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        translateFromField.setText(textToTranslate);
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
        // This will be called only once(if there's no bugs)
        for (var i : getLanguageButtonsBySide(side))
            i.setSelected(i.getText().equals(lang));

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
            _languages_.remove((_languages_.size() - 1) < 0 ? 0 : _languages_.size() - 1);
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

        // Init UI Elements
        JPanel translatorPanel;
        {
            mainFrame = new JFrame(Constants.APP_NAME);
            translatorPanel = new JPanel();
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
            mainPanel = new JPanel();
            translateFromPreferencePanel = new JPanel();
            translateFromUserLanguagesPanel = new JPanel();//--------
            translateFromPreferencePanelGL = new GridLayout(1, 3);
            translateFromUserLanguages = new ArrayList<>();
            translateFromLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(),
                    new ArrayList<>(TranslationCore.getInstance().getAvailableLanguages().keySet()));
            swapLanguages = new JButton("Swap");
            swapLanguages.setIcon(new FlatMenuArrowIcon());
            translateFromField = new JTextArea();

            //----------
            translateToPreferencePanel = new JPanel();
            translateToUserLanguagesPanel = new JPanel();//--------
            translateToPreferencePanelGL = new GridLayout(1, 3);
            translateToUserLanguages = new ArrayList<>();
            translateToLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(),
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
            translatorPanel.setLayout(new GridLayout(2, 2));

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            translatorPanel.add(translateFromPreferencePanel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            translatorPanel.add(translateToPreferencePanel, gridBagConstraints);
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.ipady = 100;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            translatorPanel.add(translateFromField, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            translatorPanel.add(translateToField, gridBagConstraints);

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(translatorPanel);

            mainFrame.add(mainPanel);
            mainFrame.setAlwaysOnTop(false);
            mainFrame.pack();
            mainFrame.setVisible(true);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setResizable(false);
        }
        SetIcon();
        AddTrayIcon();
    }

    private void SetIcon(){
        File file = new File(Constants.APP_ICON_PATH);

        if(!(new File(Constants.APP_TRAY_ICON_PATH).exists())){
            JOptionPane.showMessageDialog(mainFrame, "App icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedImage bImage = ImageIO.read(file);
            getMainFrame().setIconImage(bImage);

            //set icon on system tray, as in Mac OS X system
            if(System.getProperty("os.name").contains("Mac")){
                final Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(bImage);// - OrkhanGG TODO: test it on Mac
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void AddTrayIcon(){
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if(!(new File(Constants.APP_TRAY_ICON_PATH).exists())){
          JOptionPane.showMessageDialog(mainFrame, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(Constants.APP_TRAY_ICON_PATH);
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(image, Constants.APP_NAME, popup);
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    // info: ------------------------------------

    private void AddListeners() {

        // On TranslateField Change(When user stops typing)
        DeferredDocumentListener listener = new DeferredDocumentListener(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translatorThread = new Thread(() -> {
                    try {
                        final String from = getSelectedLanguage(TranslationSides.TS_LEFT);
                        final String to = getSelectedLanguage(TranslationSides.TS_RIGHT);

                        final String translatedText =
                                TranslationCore.getInstance().translate(TranslationCore.getInstance().getLanguageCodeByName(from), TranslationCore.getInstance().getLanguageCodeByName(to), translateFromField.getText());

                        translateToField.setText(translatedText);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                });
                translatorThread.start();

                // Update mainFrame size
                getMainFrame().pack();
            }
        }, true);
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


    }

    enum TranslationSides {
        TS_LEFT, TS_RIGHT
    }

    // Nested Classes
    private final class JDropdownButton extends JButton {

        List<JMenuItem> menuItems;
        List<String> items;
        JPopupMenu popupMenu = null;

        public JDropdownButton(String label, Icon icon, List<String> items) {

            super(label, icon);

            this.items = items;

            menuItems = new ArrayList<>();

            super.addActionListener(e ->
            {
                String s = e.getActionCommand();
                    popupMenu = new JPopupMenu();

                    for (var i : items) {
                        menuItems.add(new JMenuItem(i));
                    }
                    for (var i : menuItems)
                        popupMenu.add(i);

                    popupMenu.show(this,10,10);
            });
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
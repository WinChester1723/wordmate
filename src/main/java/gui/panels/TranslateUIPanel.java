package gui.panels;

import aws.api.DictionaryAPI;
import aws.api.TextToSpeechAPI;
import aws.api.TranslateAPI;
import com.formdev.flatlaf.icons.FlatDescendingSortIcon;
import com.formdev.flatlaf.icons.FlatFileViewFileIcon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gui.frames.GUIFrame;
import gui.utils.icons.ApplicationIcons;
import gui.utils.icons.IconManager;
import utils.ClipboardManager;
import utils.Constants;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TranslateUIPanel extends JPanel{
    // Main GUI Elements
    final static short translateFieldMaxLength = 5000;
    private GUIFrame<JFrame> ParentFrame = null;
//    private static TranslateUIPanel single_instance = null;
    private final JProgressBar mainProgressBar = null;
    private JScrollPane inputFieldScrollbar;
    private JScrollPane outputFieldScrollbar = null;
    // API Objects
    private TextToSpeechAPI textToSpeechAPI = null;
    private DictionaryAPI dictionaryAPI = null;
    private JDropdownButton inputLanguageDropdown;
    private JDropdownButton outputLanguageDropdown;
    // GUI Controls
    private List<JToggleButton> inputUserLanguages = null;
    private JButton inputReadLoudButton = null;
    private JButton inputCopyToClipboard = null;
    private JTextArea inputField = null;
    private JProgressBar inputFieldCharacterCountIndicator = null;
    private JButton swapLanguages = null;
    private List<JToggleButton> outputUserLanguages = null;
    private JTextArea outputField = null;
    private JButton outputReadLoud = null;
    private JButton outputCopyToClipboard = null;
    public TranslateUIPanel() {
        try {
            Initialize();
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
    // Methods
//    public static TranslateUIPanel getInstance() {
//        if (single_instance == null) single_instance = new TranslateUIPanel();
//
//        return single_instance;
//    }

    public void Initialize() throws BadLocationException {
        // Init other elements
        dictionaryAPI = new DictionaryAPI();

        // Init UI Elements
        JPanel translatorPanel;
        {
            translatorPanel = new JPanel();
        }

        JPanel ioHeaderPanel;
        JPanel inputFooterPanel;
        JPanel outputFooterPanel;
        JPanel ioFields;
        JPanel ioFieldsFooterPanel;

        {
            ioHeaderPanel = new JPanel();
            inputUserLanguages = new ArrayList<>();
            inputLanguageDropdown = new JDropdownButton("Add", new FlatDescendingSortIcon(), new ArrayList<>(TranslateAPI.getInstance().getAvailableLanguages().keySet()));
            swapLanguages = new JButton("Swap", IconManager.getInstance().getIcon(ApplicationIcons.ICON_SWAP));
            inputField = new JTextArea();
            inputFieldScrollbar = new JScrollPane(inputField);
            inputFooterPanel = new JPanel();
            inputReadLoudButton = new JButton("Play", IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));
            inputCopyToClipboard = new JButton("Copy to Clipboard", new FlatFileViewFileIcon());
            inputFieldCharacterCountIndicator = new JProgressBar(0, translateFieldMaxLength);
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
            ioFields.setLayout(new GridLayout(1, 2));
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
            ioFieldsFooterPanel.setLayout(new GridLayout(1, 2));
            ioFieldsFooterPanel.add(inputFooterPanel);
            ioFieldsFooterPanel.add(outputFooterPanel);
            translatorPanel.add(ioFieldsFooterPanel, BorderLayout.PAGE_END);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(translatorPanel);
        }

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
        int currentSelectedIndex = headerGetSelectedLanguageIndex(side) + 1;

        if (currentSelectedIndex >= headerGetExistingLanguages(side).size()) currentSelectedIndex = 0;

        headerSetSelectedLanguage(headerGetExistingLanguages(side).get(currentSelectedIndex), side);
    }

    private TranslationInputOutputEnum headerGetOppositeSide(TranslationInputOutputEnum side) {
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

        inputFieldCharacterCountIndicator.addChangeListener(e -> {
            inputFieldCharacterCountIndicator.setString(String.format("%d / %d", inputField.getText().length(), translateFieldMaxLength));
            inputFieldCharacterCountIndicator.update(inputFieldCharacterCountIndicator.getGraphics());
        });


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

        // On select text
        inputField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                String selection = inputField.getText().substring(inputField.getSelectionStart(),inputField.getSelectionEnd());
                if(selection.equals(null) || selection.isEmpty() || selection.isBlank()) {
                    return;
                }
                String words[] = selection.split(" ");
                selection = words[0];// Only a word may be got

                String meaningDef = null;
                try {
                    meaningDef = dictionaryAPI.RequestWordDefinitionJson("en", selection);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                final Popup p = PopupFactory.getSharedInstance().getPopup(inputField, new
                        JLabel("Here is my popup!"), MouseInfo.getPointerInfo().getLocation().x,  MouseInfo.getPointerInfo().getLocation().y);
                p.show();
                // create a timer to hide the popup later
                Timer t = new Timer(5000, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        p.hide();

                    }
                });
                t.setRepeats(false);
                t.start();

                JsonArray allData = new JsonParser().parse(meaningDef).getAsJsonArray();
                // Now Take Rates as JSON Object and capture it in a Map.
                JsonArray raw = allData.getAsJsonArray();
                for (var mainElements : raw) {
                    final String word = mainElements.getAsJsonObject().get("word").getAsString();
                    System.out.println("Word:" + word);

                    System.out.println(mainElements.getAsJsonObject().get("phonetics"));

                    JsonElement meanings = mainElements.getAsJsonObject().get("meanings");
                    System.out.println("------Meanings");
                    for (var meaning : meanings.getAsJsonArray()) {
                        final String partOfSpeech = meaning.getAsJsonObject().get("partOfSpeech").getAsString();
                        System.out.println("Part of speech:" + partOfSpeech);

                        JsonElement definitions = meaning.getAsJsonObject().get("definitions");
                        System.out.printf("Definitions(%s):\n",partOfSpeech);
                        if (definitions != null) for (var definition : definitions.getAsJsonArray()) {

                            JsonElement example = definition.getAsJsonObject().get("definition");
                            System.out.println(example);

                            JsonElement definitionSynonyms = definition.getAsJsonObject().get("synonyms");
                            if(definitionSynonyms != null) for(var definitionSynonym : definitionSynonyms.getAsJsonArray()){
                                // If exist any, will be displayed
                                System.out.println(definitionSynonym);
                            }

                            JsonElement definitionAntonyms = definition.getAsJsonObject().get("antonyms");
                            if(definitionAntonyms != null) for(var definitionAntonym : definitionAntonyms.getAsJsonArray()){
                                // If exist any, will be displayed
                                System.out.println(definitionAntonym);
                            }
                        }

                        JsonElement synonyms = mainElements.getAsJsonObject().get("synonyms");
                        System.out.println("---   Synonyms   ---");
                        if(synonyms != null) for(var synonym : synonyms.getAsJsonArray()){
                            System.out.println(synonym);
                        }
                        System.out.println("---   Synonyms End   ---");

                        JsonElement antonyms = mainElements.getAsJsonObject().get("antonyms");
                        System.out.println("---   Antonyms   ---");
                        if(antonyms != null) for(var antonym : antonyms.getAsJsonArray()){
                            System.out.println(antonym);
                        }
                        System.out.println("---   Antonyms End   ---");
                    }
                    System.out.println("-------------------");

                    //System.out.println(mainElements.getAsJsonObject().get("license"));
                    //System.out.println(mainElements.getAsJsonObject().get("sourceUrls"));
                }


            }
        });

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
            JOptionPane.showMessageDialog(this, "Either translate-From/To LanguageDropdown button's menu items don't exist! ", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Text to Speech events
        // TODO: Depending on TTS support over the languages, these buttons will be enabled/disabled!
        inputReadLoudButton.addActionListener(e -> {
            if (inputField.getText().isEmpty() || inputField.getText().isBlank()) return;

            Thread textToSpeechThread = new Thread(() -> {

                // Reset icons first
                inputReadLoudButton.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));

                inputReadLoudButton.setEnabled(false);

                if (textToSpeechAPI != null) if (textToSpeechAPI.isPlaying()) {
                    inputReadLoudButton.setEnabled(false);
                    textToSpeechAPI.RequestToStopStream();
                    inputReadLoudButton.setEnabled(true);
                    return;
                }

                // Then set which needs to be changed
                inputReadLoudButton.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_STOP));

                textToSpeechAPI = new TextToSpeechAPI();
                textToSpeechAPI.Initialize();// TODO: Load recent voice and output format (serialized ones)

                textToSpeechAPI.addOnStopListener(() -> {
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

        outputReadLoud.addActionListener(e -> {
            if (outputField.getText().isEmpty() || outputField.getText().isBlank()) return;

            Thread textToSpeechThread = new Thread(() -> {

                // Reset icons first
                outputReadLoud.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_PLAY));

                outputReadLoud.setEnabled(false);

                if (textToSpeechAPI != null) if (textToSpeechAPI.isPlaying()) {
                    outputReadLoud.setEnabled(false);
                    textToSpeechAPI.RequestToStopStream();
                    outputReadLoud.setEnabled(true);
                    return;
                }

                // Then set which needs to be changed
                outputReadLoud.setIcon(IconManager.getInstance().getIcon(ApplicationIcons.ICON_STOP));

                textToSpeechAPI = new TextToSpeechAPI();
                textToSpeechAPI.Initialize();// TODO: Load recent voice and output format (serialized ones)

                textToSpeechAPI.addOnStopListener(() -> {
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
        inputCopyToClipboard.addActionListener(e -> {
            try {
                ClipboardManager.getInstance().setClipboardText(inputField.getText());
                String originalText = inputCopyToClipboard.getText();
                inputCopyToClipboard.setText("Copied!");
                ParentFrame.getFrameTrayIcon().displayMessage(Constants.APP_NAME, "Copied:" + outputField.getText(), TrayIcon.MessageType.INFO);

                inputCopyToClipboard.setEnabled(false);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        inputCopyToClipboard.setText(originalText);
                        inputCopyToClipboard.setEnabled(true);
                    }
                }, 1000);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        outputCopyToClipboard.addActionListener(e -> {
            try {
                ClipboardManager.getInstance().setClipboardText(outputField.getText());
                String originalText = outputCopyToClipboard.getText();
                outputCopyToClipboard.setText("Copied!");

                ParentFrame.getFrameTrayIcon().displayMessage(Constants.APP_NAME, "Copied:" + outputField.getText(), TrayIcon.MessageType.INFO);

                outputCopyToClipboard.setEnabled(false);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        outputCopyToClipboard.setText(originalText);
                        outputCopyToClipboard.setEnabled(true);
                    }
                }, 1000);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    enum TranslationInputOutputEnum {
        TS_INPUT, TS_OUTPUT
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
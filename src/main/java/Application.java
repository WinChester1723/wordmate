// Authors: OrkhanGG, WinChester1723, Deusrazen

import aws.api.DictionaryAPI;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gui.core.TranslateUI;
import io.InputCore;
import utils.serialization.structures.WordDefinition;

import javax.swing.text.BadLocationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Application {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Start GUI
        try {
            TranslateUI.getInstance().Initialize();
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        // Implement Input Core
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new InputCore());


        //------------------------------------------------

        BufferedReader test = new BufferedReader(new InputStreamReader(System.in));
        String meaningDef = null;
        try {
            meaningDef = new DictionaryAPI().RequestWordDefinitionJson("en", test.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<WordDefinition> wordDefinitions = new ArrayList<>();
        JsonArray allData = new JsonParser().parse(meaningDef).getAsJsonArray();
        JsonArray raw = allData.getAsJsonArray();
        if (raw != null) for (var mainElements : raw) {
            final String word = mainElements.getAsJsonObject().get("word").getAsString();
            WordDefinition wordDefinition = new WordDefinition();
            wordDefinition.word = word;
            JsonElement phonetics = mainElements.getAsJsonObject().get("phonetics");
            if (phonetics != null) {
                var _phonetic = wordDefinition.getNewPhonetic();
                wordDefinition.phonetics = new ArrayList<>();
                for (var phonetic : phonetics.getAsJsonArray()) {
                    // text audio sourceURL
                    final String text = phonetic.getAsJsonObject().get("text") != null ? phonetic.getAsJsonObject().get("text").getAsString() : null;
                    final String audio = phonetic.getAsJsonObject().get("audio") != null ? phonetic.getAsJsonObject().get("audio").getAsString() : null;
                    final String sourceURL = phonetic.getAsJsonObject().get("sourceUrl") != null ? phonetic.getAsJsonObject().get("sourceUrl").getAsString() : null;

                    _phonetic.text = text;
                    _phonetic.audio = audio;
                    _phonetic.sourceUrl = sourceURL;
                    wordDefinition.phonetics.add(_phonetic);
                }
            }
            JsonElement meanings = mainElements.getAsJsonObject().get("meanings");
            if (meanings != null) {
                var _meaning = wordDefinition.getNewMeaning();
                wordDefinition.meanings = new ArrayList<>();
                for (var meaning : meanings.getAsJsonArray()) {
                    final String partOfSpeech = meaning.getAsJsonObject().get("partOfSpeech") != null ? meaning.getAsJsonObject().get("partOfSpeech").getAsString() : null;
                    _meaning.partOfSpeech = partOfSpeech;
                    JsonElement definitions = meaning.getAsJsonObject().get("definitions");
                    if (definitions != null) {
                        var _definition = _meaning.getNewDefinition();
                        _definition.synonyms = new ArrayList<>();
                        _definition.antonyms = new ArrayList<>();
                        for (var definition : definitions.getAsJsonArray()) {
                            JsonElement example = definition.getAsJsonObject().get("definition");
                            _meaning.definitions = new ArrayList<>();
                            if (example != null) _definition.definition = example.getAsString();
                            else _definition.definition = null;
                            JsonElement definitionSynonyms = definition.getAsJsonObject().get("synonyms");
                            if (definitionSynonyms != null) {
                                for (var definitionSynonym : definitionSynonyms.getAsJsonArray()) {
                                    if (definitionSynonym != null)
                                        _definition.synonyms.add(definitionSynonym.getAsString());
                                }
                            }
                            JsonElement definitionAntonyms = definition.getAsJsonObject().get("antonyms");
                            if (definitionAntonyms != null) {
                                for (var definitionAntonym : definitionAntonyms.getAsJsonArray()) {
                                    if (definitionAntonym != null)
                                        _definition.antonyms.add(definitionAntonym.getAsString());
                                }
                            }
                            _meaning.definitions.add(_definition);
                        }
                    }
                }
                JsonElement synonyms = mainElements.getAsJsonObject().get("synonyms");
                if (synonyms != null) for (var synonym : synonyms.getAsJsonArray()) {
                    System.out.println(synonym);
                }
                JsonElement antonyms = mainElements.getAsJsonObject().get("antonyms");
                if (antonyms != null) for (var antonym : antonyms.getAsJsonArray()) {
                    System.out.println(antonym);
                }
                wordDefinition.meanings.add(_meaning);
            }
            wordDefinitions.add(wordDefinition);
        }

        for (var word : wordDefinitions) {
            System.out.println(word.word);
            if (word.phonetics.size() > 0) for (var phonetic : word.phonetics) {
                System.out.println("NEW PHONETIC");
                System.out.println("\t phonetic text:" + phonetic.text);
                System.out.println("\t phonetic audio:" + phonetic.audio);
                System.out.println("\t phonetic sourceURL:" + phonetic.sourceUrl);
            }
            if (word.meanings.size() > 0) for (var meaning : word.meanings) {
                System.out.println("\t\tmeaning-> part of speech:" + meaning.partOfSpeech);
                for (var defs : meaning.definitions) {
                    System.out.println("\t\t\tmeaning->definition def:" + defs.definition);
                }
            }
        }
    }
}
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
        String meaningDef = new DictionaryAPI().RequestWordDefinitionJson("en", test.readLine());

        System.out.println(meaningDef);

        List<WordDefinition> wordDefinitions = new ArrayList<>();

        JsonArray allData = new JsonParser().parse(meaningDef).getAsJsonArray();

// >>>>>>>>>>>>---------------------sprosi a tak mojno delat?___________________<<<<<<<<<<
//        JsonArray allData2 = new JsonParser().parseString(meaningDef).getAsJsonArray();

        // Now Take Rates as JSON Object and capture it in a Map.
        JsonArray raw = allData.getAsJsonArray();
        for (var mainElements : raw) {
            final String word = mainElements.getAsJsonObject().get("word").getAsString();

            WordDefinition wordDefinition = new WordDefinition();

            wordDefinition.word = word;

            JsonElement phonetics = mainElements.getAsJsonObject().get("phonetics");

            System.out.println("Phonetics -----------");
            if (phonetics != null) {
                wordDefinition.phonetics = new ArrayList<>();

                for (var phonetic : phonetics.getAsJsonArray()) {
                    // text audio sourceURL
                    final String text = phonetic.getAsJsonObject().get("text").getAsString();
                    final String audio = phonetic.getAsJsonObject().get("audio").getAsString();
                    final String sourceURL = phonetic.getAsJsonObject().get("sourceUrl").getAsString();

                    wordDefinition.phonetics.add(wordDefinition.createPhonetic(text, audio, sourceURL));
                }
            }
            System.out.println("Phonetics END  -----------");


            JsonElement meanings = mainElements.getAsJsonObject().get("meanings");

            System.out.println("------Meanings");

            if (meanings == null) {
                wordDefinition.meanings = new ArrayList<>();

                for (var meaning : meanings.getAsJsonArray()) {
                    final String partOfSpeech = meaning.getAsJsonObject().get("partOfSpeech").getAsString();
//                    System.out.println("Part of speech:" + partOfSpeech);

                    JsonElement definitions = meaning.getAsJsonObject().get("definitions");
//                    System.out.printf("Definitions(%s):\n", partOfSpeech);
                    if (definitions != null) {
                        wordDefinition.definitions = new ArrayList<>();

                        for (var definition : definitions.getAsJsonArray()) {

                            JsonElement example = definition.getAsJsonObject().get("definition");
                            System.out.println(example);

                            JsonElement definitionSynonyms = definition.getAsJsonObject().get("synonyms");
                            if (definitionSynonyms != null) {
                                wordDefinition.definitionSynonyms = new ArrayList<>();
                                for (var definitionSynonym : definitionSynonyms.getAsJsonArray()) {
                                    // If exist any, will be displayed
                                    System.out.println(definitionSynonym);
                                }
                            }

                            JsonElement definitionAntonyms = definition.getAsJsonObject().get("antonyms");
                            if (definitionAntonyms != null) {
                                wordDefinition.definitionAntonyms = new ArrayList<>();
                                for (var definitionAntonym : definitionAntonyms.getAsJsonArray()) {
                                    // If exist any, will be displayed
                                    System.out.println(definitionAntonym);
                                }
                            }
                            wordDefinition.definitions.add(wordDefinition.createDefinition(partOfSpeech,
                                    (List<String>) definitionSynonyms, (List<String>) definitionAntonyms));
                        }
                    }
                    wordDefinition.meanings.add(wordDefinition.createMeaning(partOfSpeech));

                }

                JsonElement synonyms = mainElements.getAsJsonObject().get("synonyms");
                System.out.println("---   Synonyms   ---");
                if (synonyms != null) for (var synonym : synonyms.getAsJsonArray()) {
                    System.out.println(synonym);
                }
                System.out.println("---   Synonyms End   ---");

                JsonElement antonyms = mainElements.getAsJsonObject().get("antonyms");
                System.out.println("---   Antonyms   ---");
                if (antonyms != null) for (var antonym : antonyms.getAsJsonArray()) {
                    System.out.println(antonym);
                }
                System.out.println("---   Antonyms End   ---");
            }
            System.out.println("-------------------");

            wordDefinitions.add(wordDefinition);
            //System.out.println(mainElements.getAsJsonObject().get("license"));
            //System.out.println(mainElements.getAsJsonObject().get("sourceUrls"));
        }


        System.out.printf("WordDefintion Size: %d, Phonetics that are available for this word: %d",
                wordDefinitions.size(), wordDefinitions.get(0).phonetics.size());

    }
}
// Authors: OrkhanGG, WinChester1723, Deusrazen

import aws.api.DictionaryAPI;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gui.core.TranslateUI;
import io.InputCore;

import javax.swing.text.BadLocationException;
import java.io.*;


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

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(meaningDef));
        meaningDef = (String) ois.readObject();
        ois.close();

//        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(meaningDef.getBytes()));
//        meaningDef = (String) ois.readObject();
//        ois.close();

        System.out.println(meaningDef);

        JsonArray allData = new JsonParser().parse(meaningDef).getAsJsonArray();

// >>>>>>>>>>>>---------------------sprosi a tak mojno delat?___________________<<<<<<<<<<
//        JsonArray allData2 = new JsonParser().parseString(meaningDef).getAsJsonArray();

        // Now Take Rates as JSON Object and capture it in a Map.
        JsonArray raw = allData.getAsJsonArray();
        for (var mainElements : raw) {
            final String word = mainElements.getAsJsonObject().get("word").getAsString();
            System.out.println("Word:" + word);

            JsonElement phonetics = mainElements.getAsJsonObject().get("phonetics");

            System.out.println("Phonetics -----------");
            if (phonetics != null) for (var phonetic : phonetics.getAsJsonArray()) {
                // text audio sourceURL

                final String text = phonetic.getAsJsonObject().get("text").getAsString();
                System.out.println("Text:" + text);
                final String audio = phonetic.getAsJsonObject().get("audio").getAsString();
                System.out.println("Audio:" + audio);
                final String sourceURL = phonetic.getAsJsonObject().get("sourceUrl").getAsString();
                System.out.println("SourceURL:" + sourceURL);
            }
            System.out.println("Phonetics END  -----------");

            JsonElement meanings = mainElements.getAsJsonObject().get("meanings");
            System.out.println("------Meanings");
            for (var meaning : meanings.getAsJsonArray()) {
                final String partOfSpeech = meaning.getAsJsonObject().get("partOfSpeech").getAsString();
                System.out.println("Part of speech:" + partOfSpeech);

                JsonElement definitions = meaning.getAsJsonObject().get("definitions");
                System.out.printf("Definitions(%s):\n", partOfSpeech);
                if (definitions != null) for (var definition : definitions.getAsJsonArray()) {

                    JsonElement example = definition.getAsJsonObject().get("definition");
                    System.out.println(example);

                    JsonElement definitionSynonyms = definition.getAsJsonObject().get("synonyms");
                    if (definitionSynonyms != null) for (var definitionSynonym : definitionSynonyms.getAsJsonArray()) {
                        // If exist any, will be displayed
                        System.out.println(definitionSynonym);
                    }

                    JsonElement definitionAntonyms = definition.getAsJsonObject().get("antonyms");
                    if (definitionAntonyms != null) for (var definitionAntonym : definitionAntonyms.getAsJsonArray()) {
                        // If exist any, will be displayed
                        System.out.println(definitionAntonym);
                    }
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

            //System.out.println(mainElements.getAsJsonObject().get("license"));
            //System.out.println(mainElements.getAsJsonObject().get("sourceUrls"));
        }
    }
}
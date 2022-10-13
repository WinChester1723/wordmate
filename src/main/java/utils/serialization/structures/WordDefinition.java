package utils.serialization.structures;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WordDefinition {
    @SerializedName("word")
    public String word = null;
    public List<Phonetic> phonetics = null;
    public List<Meaning> meanings = null;

//    de-serialization start
    //add text origin
    private final class Phonetic implements Serializable {
        final String text;
        final String audio;
        final String sourceUrl;

    public Phonetic(String text, String audio, String sourceUrl) {
        this.text = text;
        this.audio = audio;
        this.sourceUrl = sourceUrl;
    }
}

    private final class Meaning implements Serializable{
        final String partOfSpeech;
        List<Definition> definitions = null;

        public Meaning(String partOfSpeech, List<Definition> definitions) {
            this.partOfSpeech = partOfSpeech;
            this.definitions = definitions;
        }

        private final class Definition implements Serializable{
            String definition;
            List<String> synonym;
            List<String> antonyms;

            public Definition(String definition, List<String> synonym, List<String> antonyms) {
                this.definition = definition;
                this.synonym = synonym;
                this.antonyms = antonyms;
            }
        }
    }
}

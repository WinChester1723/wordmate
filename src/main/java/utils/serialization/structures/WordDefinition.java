package utils.serialization.structures;

import java.util.List;

public class WordDefinition {
    public String word = null;
    public List<Phonetic> phonetics = null;
    public List<Meaning> meanings = null;

    public List<Meaning.Definition> definitions = null;

    public List<Meaning.DefinitionSynonyms> definitionSynonyms = null;

    public List<Meaning.DefinitionAntonyms> definitionAntonyms = null;

    public Phonetic createPhonetic(String text, String audio, String sourceUrl) {
        Phonetic phonetic = new Phonetic(text, audio, sourceUrl);
        return phonetic;
    }

    public Meaning createMeaning(String partOfSpeech, List<Meaning.Definition> definitions){
        Meaning meaning = new Meaning(partOfSpeech, definitions);
        return meaning;
    }

    public Meaning createMeaning(String partOfSpeech){
        Meaning meaning = new Meaning(partOfSpeech);
        return meaning;
    }

    public Meaning.Definition createDefinition(String definitions, List<String> synonym, List<String> antonyms){
        Meaning.Definition definition = new Meaning.Definition(definitions, synonym, antonyms);
        return definition;
    }

    public Meaning.DefinitionSynonyms createDefinitionSynonyms(){
        Meaning.DefinitionSynonyms definitionSynonyms = new Meaning.DefinitionSynonyms();
        return definitionSynonyms;
    }

    public Meaning.DefinitionAntonyms createDefinitionAntonyms(){
        Meaning.DefinitionAntonyms definitionAntonyms = new Meaning.DefinitionAntonyms();
        return definitionAntonyms;
    }

    public final class Phonetic {
        public Phonetic(String text, String audio, String sourceUrl) {
            this.text = text;
            this.audio = audio;
            this.sourceUrl = sourceUrl;
        }

        String text;
        String audio;
        String sourceUrl;
    }

    public final class Meaning {
        final String partOfSpeech;
        List<Definition> definitions = null;

        public Meaning(String partOfSpeech, List<Definition> definitions) {
            this.partOfSpeech = partOfSpeech;
            this.definitions = definitions;
        }

        public Meaning(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        public static final class Definition {
            String definition;
            List<String> synonym;
            List<String> antonyms;

            public Definition(String definition, List<String> synonym, List<String> antonyms) {
                this.definition = definition;
                this.synonym = synonym;
                this.antonyms = antonyms;
            }
        }

        public static final class DefinitionSynonyms{
        }
        public static final class DefinitionAntonyms{
        }
    }
}

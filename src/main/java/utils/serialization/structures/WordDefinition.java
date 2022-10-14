package utils.serialization.structures;

import java.util.List;

public class WordDefinition {
    public String word = null;
    public List<Phonetic> phonetics = null;
    public List<Meaning> meanings = null;

    public Phonetic getNewPhonetic(){
        return new Phonetic();
    }

    public Meaning getNewMeaning(){
        return new Meaning();
    }

    //Inner classes <Phonetic>
    public final class Phonetic {
        public String text;
        public String audio;
        public String sourceUrl;
    }

    //Inner classes <Meaning>
    public final class Meaning {
        public String partOfSpeech = null;
        public List<Definition> definitions = null;
        public List<DefinitionSynonyms> definitionSynonyms = null;
        public List<DefinitionAntonyms> definitionAntonyms = null;

        public Definition getNewDefinition(){
            return new Definition();
        }
        public DefinitionSynonyms getNewDefSyn(){
            return new DefinitionSynonyms();
        }
        public DefinitionAntonyms getNewDefAnt(){
            return new DefinitionAntonyms();
        }
        //>>>>>>>>>>>>>>>>>>>---------------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<
        public final class Definition {
            public String definition;
            public List<String> synonyms;
            public List<String> antonyms;
        }
        //>>>>>>>>>>>>>>>>>>>---------------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<
        public final class DefinitionSynonyms {
        }
        //>>>>>>>>>>>>>>>>>>>---------------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<
        public final class DefinitionAntonyms {
        }
    }
}

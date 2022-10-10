// Author: OrkhanGG
// Created: 10/10/2022
// Purpose: TranslateAPI

package aws.api;

import aws.credentials.AWSCredentialsManager;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TranslateAPI {
    private static TranslateAPI single_instance = null;
    private static AmazonTranslate amazonTranslate = null;
    private static Map<String, String> languageCodeMap = null;

    public static TranslateAPI getInstance() {
        if (single_instance == null) single_instance = new TranslateAPI();

        if (languageCodeMap == null) {
            languageCodeMap = new LinkedHashMap<>();
            configureLanguageCodes();
        }

        if (amazonTranslate == null) {
            // For more information, please refer to AWSCredentialsManager
            amazonTranslate = AmazonTranslateClient.builder().withCredentials(
                    new AWSStaticCredentialsProvider(AWSCredentialsManager.getInstance()))
                    .withRegion(AWSCredentialsManager.getInstance().getRegion()).build();
        }

        return single_instance;
    }

    private static void configureLanguageCodes() {
        languageCodeMap.put("Afrikaans", "af");
        languageCodeMap.put("Irish", "ga");
        languageCodeMap.put("Albanian", "sq");
        languageCodeMap.put("Italian", "it");
        languageCodeMap.put("Arabic", "ar");
        languageCodeMap.put("Japanese", "ja");
        languageCodeMap.put("Azerbaijani", "az");
        languageCodeMap.put("Kannada", "kn");
        languageCodeMap.put("Basque", "eu");
        languageCodeMap.put("Korean", "ko");
        languageCodeMap.put("Bengali", "bn");
        languageCodeMap.put("Latin", "la");
        languageCodeMap.put("Belarusian", "be");
        languageCodeMap.put("Latvian", "lv");
        languageCodeMap.put("Bulgarian", "bg");
        languageCodeMap.put("Lithuanian", "lt");
        languageCodeMap.put("Catalan", "ca");
        languageCodeMap.put("Macedonian", "mk");
        languageCodeMap.put("Malay", "ms");
        languageCodeMap.put("Maltese", "mt");
        languageCodeMap.put("Croatian", "hr");
        languageCodeMap.put("Norwegian", "no");
        languageCodeMap.put("Czech", "cs");
        languageCodeMap.put("Persian", "fa");
        languageCodeMap.put("Danish", "da");
        languageCodeMap.put("Polish", "pl");
        languageCodeMap.put("Dutch", "nl");
        languageCodeMap.put("Portuguese", "pt");
        languageCodeMap.put("English", "en");
        languageCodeMap.put("Romanian", "ro");
        languageCodeMap.put("Esperanto", "eo");
        languageCodeMap.put("Russian", "ru");
        languageCodeMap.put("Estonian", "et");
        languageCodeMap.put("Serbian", "sr");
        languageCodeMap.put("Filipino", "tl");
        languageCodeMap.put("Slovak", "sk");
        languageCodeMap.put("Finnish", "fi");
        languageCodeMap.put("Slovenian", "sl");
        languageCodeMap.put("French", "fr");
        languageCodeMap.put("Spanish", "es");
        languageCodeMap.put("Galician", "gl");
        languageCodeMap.put("Swahili", "sw");
        languageCodeMap.put("Georgian", "ka");
        languageCodeMap.put("Swedish", "sv");
        languageCodeMap.put("German", "de");
        languageCodeMap.put("Tamil", "ta");
        languageCodeMap.put("Greek", "el");
        languageCodeMap.put("Telugu", "te");
        languageCodeMap.put("Gujarati", "gu");
        languageCodeMap.put("Thai", "th");
        languageCodeMap.put("Turkish", "tr");
        languageCodeMap.put("Hebrew", "iw");
        languageCodeMap.put("Ukrainian", "uk");
        languageCodeMap.put("Hindi", "hi");
        languageCodeMap.put("Urdu", "ur");
        languageCodeMap.put("Hungarian", "hu");
        languageCodeMap.put("Vietnamese", "vi");
        languageCodeMap.put("Icelandic", "is");
        languageCodeMap.put("Welsh", "cy");
        languageCodeMap.put("Indonesian", "id");
        languageCodeMap.put("Yiddish", "yi");
    }

    public String translate(String langFrom, String langTo, String text) throws IOException {

        if ((langFrom == "" || langFrom == null) || (langTo == "" || langTo == null) || (text == "" || text == null))
            return "";

        TranslateTextRequest request = new TranslateTextRequest().withText(text).withSourceLanguageCode(langFrom).withTargetLanguageCode(langTo);
        TranslateTextResult result = amazonTranslate.translateText(request);

        return result.getTranslatedText();
    }

    public Map<String, String> getAvailableLanguages() {
        return languageCodeMap;
    }

    public String getLanguageCodeByName(String language) {
        return languageCodeMap.get(language);
    }
}

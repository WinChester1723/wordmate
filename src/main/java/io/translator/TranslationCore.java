package main.java.io.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TranslationCore {
    private static TranslationCore single_instance = null;
    private static Map<String, String> languageCodeMap;

    private TranslationCore() {
        languageCodeMap = new LinkedHashMap<>();
        configureLanguageCodes();
    }

    public static TranslationCore getInstance() {
        if (single_instance == null) single_instance = new TranslationCore();

        return single_instance;
    }

    public static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbyGdtZ3uVFKiZtTtxfsqnKngalPHmJBSBNQDWMN7NK3PhkIe93iAbYbNc9Pue62UMsR/exec" + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&target=" + langTo + "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static Map<String, String> getAvailableLanguages() {
        return languageCodeMap;
    }

    public static String getLanguageCodeByName(String language) {
        return languageCodeMap.get(language);
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
}

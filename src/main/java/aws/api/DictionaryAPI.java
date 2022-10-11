package aws.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DictionaryAPI {

    private String JSONStream = null;

    // Request word definition
    public String RequestWordDefinitionJson(String languageCode, String word) throws IOException {
        String urlStr = "https://api.dictionaryapi.dev/api/v2/entries/" +
                languageCode+"/"+ word;
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

        JSONStream = response.toString();

        return JSONStream;
    }

    public String getJSONStream() {
        return JSONStream;
    }

    public class DictionaryElement{

    }
}

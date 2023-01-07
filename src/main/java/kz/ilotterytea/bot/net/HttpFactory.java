package kz.ilotterytea.bot.net;

import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.net.models.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The HTTP factory.
 * @author ilotterytea
 * @since 1.0
 */
public class HttpFactory {

    /**
     * Send the GET request.
     * @param url The URL.
     * @return Response if everything is OK, otherwise it may return Response without a response string or null if there was an error.
     */
    public static Response sendGETRequest(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection c = (HttpURLConnection) obj.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("User-Agent", SharedConstants.USER_AGENT);

            int responseCode = c.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                br.close();

                return new Response(responseCode, c.getRequestMethod(), response.toString());
            } else {
                return new Response(responseCode, c.getRequestMethod(), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

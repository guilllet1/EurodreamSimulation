package com.eurodreamsimulation.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiClient {

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream input = GeminiClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) CONFIG.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String analyserTirage(String descriptionTirage) {
        String apiKey = CONFIG.getProperty("gemini.api.key");
        String model = CONFIG.getProperty("gemini.model", "gemini-3-pro-preview");

        if (apiKey == null || apiKey.equals("VOTRE_CLE_API_GEMINI_ICI")) {
            return "⚠️ Analyse impossible : Clé API Gemini manquante dans config.properties";
        }

        try {
            // 1. Construction du Prompt
            String prompt = "Tu es un expert en statistiques de loterie EuroDreams. " +
                    "Analyse ce dernier résultat tombé : " + descriptionTirage + ". " +
                    "Donne un commentaire court (max 3 phrases) sur la rareté de cette combinaison " +
                    "(ex: beaucoup de pairs/impairs, suite de nombres, écarts...). Sois factuel et précis. Et donne une analyse sur la répartition des gains";

            // 2. Construction du JSON Body
            // Structure: { "contents": [{ "parts": [{ "text": "..." }] }] }
            JSONObject textPart = new JSONObject().put("text", prompt);
            JSONObject parts = new JSONObject().put("parts", new JSONArray().put(textPart));
            JSONObject content = new JSONObject().put("contents", new JSONArray().put(parts));

            // 3. Envoi de la requête
            String url = String.format(API_URL_TEMPLATE, model, apiKey);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(content.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Analyse de la réponse
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                // Chemin : candidates[0].content.parts[0].text
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                return "Erreur API Gemini (" + response.statusCode() + ") : " + response.body();
            }

        } catch (Exception e) {
            return "Erreur lors de l'analyse IA : " + e.getMessage();
        }
    }
}